package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.BadRequestException;
import com.flowzapi.flowz_api_builder.exception.ProjectNotExistsException;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.project.ProjectInput;
import com.flowzapi.flowz_api_builder.model.project.ProjectUpdateInput;
import com.flowzapi.flowz_api_builder.repos.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.flowzapi.flowz_api_builder.model.ProjectBuilder.aProject;

@Slf4j
@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Lazy
    @Autowired
    private FlowService flowService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String REDIS_PROJECTS_KEY = "user-projects:";


    /**
     *
     * @param userId - The user ID
     * @return - the redis key build from REDIS_PROJECTS_KEY + userId
     */
    private String getRedisKey(String userId) {
        return REDIS_PROJECTS_KEY + userId;
    }

    /**
     *
     * @param redisKey - The redis key
     * @param projectId - The project id that we want to cache
     * @param projectValue - The project data that will be cached
     * @return - This function cache the project to the user project map in the redis DB
     */
    private void cacheProjectField(String redisKey, String projectId, Object projectValue) {
        try {
            String jsonValue = (projectValue instanceof String) ?
                    (String) projectValue : objectMapper.writeValueAsString(projectValue);

            redisTemplate.opsForHash().put(redisKey, projectId, jsonValue);
            redisTemplate.expire(redisKey, Duration.ofHours(1));
        } catch (Exception e) {
            log.error("Failed to update cache field: " + projectId + " for key: " + redisKey, e);
        }
    }

    /**
     *
     * @param redisKey - The redis key of the current user projects map
     * @param isFullList - "true" or "false" - true means the objects in the map are the whole objects and false not
     */
    private void setFullListCachedStatus(String redisKey, boolean isFullList) {
        redisTemplate.opsForHash().put(redisKey, "FULL_LIST", String.valueOf(isFullList));
        redisTemplate.expire(redisKey, Duration.ofHours(1));
    }

    /**
     *
     * @param userId - The current user ID
     * @param projectId - The project ID that we want to get its info
     * @return - returns the project with projectId from the projects cache hash map
     */
    public Project getCachedProject(String userId, String projectId) {
        String redisKey = getRedisKey(userId);

        String stringProject = (String) redisTemplate.opsForHash().get(redisKey, projectId);

        if (stringProject == null) {
            return null;
        }

        try {
            return objectMapper.readValue(stringProject, Project.class);
        } catch (Exception e) {
            log.error("Failed to parse project JSON from cache for project: " + projectId, e);
            return null;
        }
    }

    /**
     *
     * @param projectId - The project ID to look for in Redis/MongoDB
     * @param userId - The current user ID
     * @return - The project that has the id equals to projectId
     */
    public Project findById(String projectId, String userId) {
        Project project = getCachedProject(userId, projectId);
        if(project == null){
            project = projectRepository.findById(projectId).orElseThrow(
                    () -> new ProjectNotExistsException("Project not exists")
            );

            if(!project.getUserId().equals(userId))
                throw new UserNotAllowedException("User not allowed to update project");

            try{
                String redisKey = getRedisKey(userId);

                cacheProjectField(redisKey, projectId, project);
                setFullListCachedStatus(redisKey, false);
            }
            catch (Exception e){
                log.error("Failed to parse project JSON from cache for project: " + projectId, e);
            }

        }

        return project;
    }

    /**
     *
     * @param userId - The user ID
     * @return - Returns all the current user projects from Redis
     */
    public List<ProjectDTO> getAllProjectsFromCache(String userId) {
        String redisKey = getRedisKey(userId);

        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

        if (entries == null || entries.isEmpty()) {
            return null;
        }

        if("EMPTY".equals(entries.get("STATUS")))
            return new ArrayList<>();

        if (!"true".equals(entries.get("FULL_LIST"))) {
            return null;
        }

        entries.remove("FULL_LIST");
        entries.remove("STATUS");

        return entries.values().stream()
                .map(value -> {
                    try {
                        return objectMapper.readValue((String) value, Project.class).convertToDTO();
                    } catch (Exception e) {
                        log.error("Failed to parse project JSON", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     *
     * @param userId - The current user ID
     * @return - Returns all the current user projects
     */
    public List<ProjectDTO> findByUserId(String userId) {
        List<ProjectDTO> cachedProjectsDTO = getAllProjectsFromCache(userId);

        if(cachedProjectsDTO != null)
            return cachedProjectsDTO;

        List<ProjectDTO> projectDTOS = new ArrayList<>();

        List<Project> projects = projectRepository.findByUserId(userId);
        Map<String, String> cachedProjectsMap = new HashMap<>();

        for (Project project : projects) {
            if(!project.getUserId().equals(userId))
                throw new UserNotAllowedException("User not allowed");

            ProjectDTO currentDTO = project.convertToDTO();
            projectDTOS.add(currentDTO);
            try{
                String jsonProject = objectMapper.writeValueAsString(project);
                cachedProjectsMap.put(project.getId(), jsonProject);
            }catch(Exception e){
                log.error("Failed to parse project from cache", e);
            }
        }

        String redisKey = getRedisKey(userId);
        if(projects.isEmpty()){
            redisTemplate.opsForHash().put(redisKey, "STATUS", "EMPTY");
        }
        else {
            redisTemplate.opsForHash().putAll(redisKey, cachedProjectsMap);
            setFullListCachedStatus(redisKey, true);
        }

        redisTemplate.expire(REDIS_PROJECTS_KEY + userId, Duration.ofHours(1));

        return projectDTOS;
    }

    /**
     *
     * @param projectInput - The new project data
     * @param userId - The project creator ID
     * @return - Creates new project, saves it to the DB and Redis and returns its data
     */
    public ProjectDTO createProject(ProjectInput projectInput, String userId) {
        Project project = aProject()
                .withUserId(userId)
                .withProjectName(projectInput.getProjectName()).build();

        Project newProject = projectRepository.save(project);

        try{
            String redisKey = getRedisKey(userId);
            cacheProjectField(redisKey, newProject.getId(), newProject);
            setFullListCachedStatus(redisKey, false);
            redisTemplate.opsForHash().delete(redisKey, "STATUS");
        }
        catch (Exception e){
            log.error("Failed to parse project JSON from cache for project: " + project.getId(), e);
        }

        return newProject.convertToDTO();
    }

    /**
     *
     * @param projectInput - The updated project data
     * @param userId - The project creator ID
     * @return - Updates existing project in the DB and Redis and returns its data
     */
    public ProjectDTO updateProject(ProjectUpdateInput projectInput, String userId) {
        if(projectInput.getProjectName() == null || projectInput.getProjectName().equals(""))
            throw new BadRequestException("project name is required", HttpStatus.BAD_REQUEST);

        Project project = this.findById(projectInput.getProjectId(), userId);

        project.setProjectName(projectInput.getProjectName());
        Project updatedProject = projectRepository.save(project);

        String redisKey = getRedisKey(userId);

        try{

            cacheProjectField(redisKey, project.getId(), updatedProject);
        }
        catch(Exception e){
            log.error("Failed to parse project JSON from cache for project: " + project.getId(), e);
        }

        return updatedProject.convertToDTO();
    }

    /**
     *
     * @param projectId - The ID
     * @param userId - The project creator ID
     * @return - Deletes the project in the DB and Redis
     */
    public void deleteProject(String projectId, String userId) {
        Project project = this.findById(projectId,  userId);

        flowService.deleteFlowByProjectId(projectId);

        projectRepository.delete(project);

        String redisKey = getRedisKey(userId);

        try{
            redisTemplate.opsForHash().delete(redisKey, project.getId());
            setFullListCachedStatus(redisKey, false);
        }
        catch(Exception e){
            log.error("Failed to parse project JSON from cache for project: " + project.getId(), e);
        }
    }
}
