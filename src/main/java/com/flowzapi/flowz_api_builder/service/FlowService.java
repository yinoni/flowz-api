package com.flowzapi.flowz_api_builder.service;

import com.flowzapi.flowz_api_builder.exception.BadRequestException;
import com.flowzapi.flowz_api_builder.exception.FlowNotFound;
import com.flowzapi.flowz_api_builder.exception.SyncException;
import com.flowzapi.flowz_api_builder.exception.UserNotAllowedException;
import com.flowzapi.flowz_api_builder.model.Flow;
import com.flowzapi.flowz_api_builder.model.FlowBuilder;
import com.flowzapi.flowz_api_builder.model.Project;
import com.flowzapi.flowz_api_builder.model.Step;
import com.flowzapi.flowz_api_builder.model.flow.*;
import com.flowzapi.flowz_api_builder.model.project.ProjectDTO;
import com.flowzapi.flowz_api_builder.model.step.StepRequest;
import com.flowzapi.flowz_api_builder.repos.FlowRepository;
import com.flowzapi.flowz_api_builder.repos.projections.FlowOwnerIdProjection;
import com.flowzapi.flowz_api_builder.repos.projections.FlowStepsProjection;
import com.flowzapi.flowz_api_builder.utils.JsonFlattener;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.flowzapi.flowz_api_builder.model.FlowBuilder.aFlow;
import static com.flowzapi.flowz_api_builder.model.StepBuilder.aStep;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowService {
    private final FlowRepository flowRepository;
    private final ProjectService projectService;
    private final MongoTemplate mongoTemplate;
    private final RedisTemplate redisTemplate;
    private final String FLOW_REDIS_KEY = "project-flows:";
    private final Duration GLOBAL_DURATION =  Duration.ofHours(1);
    private final ObjectMapper objectMapper;

    /**
     *
     * @param projectId - The project ID
     * @return - Generate the redis key with the projectId and the FLOW_REDIS_KEY
     */
    public String getRedisKey(String projectId) {
        return  FLOW_REDIS_KEY + projectId;
    }

    /**
     *
     * @param projectId - The project ID for the redis key
     * @param flowId - The flow ID
     * @return - The flow that exists in the cache. If not -> null
     */
    public FlowDTO getCachedFlow(String projectId, String flowId) {
        String redisKey = getRedisKey(projectId);
        try{
            String redisValue = (String) redisTemplate.opsForHash().get(redisKey, flowId);

            if (redisValue == null) {
                return null;
            }

            return objectMapper.readValue(redisValue, FlowDTO.class);
        }
        catch(Exception e){
            log.error("Failed to parse flow JSON from cache for flowId: " + flowId, e);
            return null;
        }
    }

    /**
     *
     * @param newFlowDTO - The updated flow
     */
    public void updateCachedFlow(FlowDTO newFlowDTO){
        try{
            String redisKey = getRedisKey(newFlowDTO.getProjectId());
            Object currentFullListStatus = redisTemplate.opsForHash().get(redisKey, "FULL_LIST");

            String stringFlowDTO = objectMapper.writeValueAsString(newFlowDTO);

            boolean shouldBeFullList = "true".equals(currentFullListStatus);

            redisTemplate.opsForHash().put(redisKey, newFlowDTO.getId(), stringFlowDTO);
            redisTemplate.opsForHash().delete(redisKey, "STATUS");
            setFullListCachedStatus(newFlowDTO.getProjectId(), shouldBeFullList);
            updateExpire(redisKey);
        }
        catch(Exception e){
            log.error("Failed to fetch the json object: createFlow Exception: ", e);
        }
    }

    /**
     *
     * @param projectId - The project ID
     * @param isFullList - Boolean flag that says if the list of flows is full or not
     */
    public void setFullListCachedStatus(String projectId, boolean isFullList){
        String redisKey = getRedisKey(projectId);
        redisTemplate.opsForHash().put(redisKey, "FULL_LIST", String.valueOf(isFullList));
    }

    /**
     *
     * @param flowId - The flow ID
     * @param projectId - The project ID for the redis key
     * @param newTime - The new time that the flow modified
     *                This fucntion updates the lastModified field in the cache
     */
    public void updateLastModifiedInCache(String flowId, String projectId, Instant newTime){
        FlowDTO flowDTO = getCachedFlow(projectId, flowId);

        if(flowDTO != null){
            flowDTO.setLastModified(newTime);
            updateCachedFlow(flowDTO);
        }
    }

    /**
     *
     * @param redisKey - The redis key
     *                 This function set the expire TTL for the redisKey value
     */
    public void updateExpire(String redisKey){
        redisTemplate.expire(redisKey, GLOBAL_DURATION);
    }

    /**
     *
     * @param flowId - The flow id
     * @param userId - The current user ID
     * @param projectId - The project ID for the redis key
     * @return - Returns the flow information by its ID
     */
    public FlowDTO findById(String projectId, String flowId, String userId) {
        FlowDTO flowDTO = getCachedFlow(projectId, flowId);

        if(flowDTO != null) {
            isUserAllowed(flowDTO.getOwnerId(), userId);
            return flowDTO;
        }

        flowDTO = flowRepository.findById(flowId)
                .orElseThrow(() -> new FlowNotFound("The flow is not found"))
                .convertToDTO();

        isUserAllowed(flowDTO.getOwnerId(), userId);

        try {
            String redisKey = getRedisKey(projectId);
            String stringDTO = objectMapper.writeValueAsString(flowDTO);

            redisTemplate.opsForHash().put(redisKey, flowId, stringDTO);
            redisTemplate.opsForHash().put(redisKey, "FULL_LIST", "false");
            updateExpire(redisKey);
        } catch (Exception e) {
            log.error("Failed to fetch the json object: getCachedFlow Exception: ", e);
        }

        return flowDTO;
    }

    /**
     *
     * @param projectId - The project ID for the redis key
     * @return - Returns all the flows that attached to the projectId. If the list is empty, returns null
     *  If the STATUS flag is EMPTY -> empty list
     */
    public List<FlowDTO> getProjectCachedFlows(String projectId) {
        String redisKey = getRedisKey(projectId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(redisKey);

        if(entries == null || entries.isEmpty())
            return null;

        if("EMPTY".equals(entries.get("STATUS")))
            return new ArrayList<>();

        if (!"true".equals(entries.get("FULL_LIST")))
            return null;


        entries.remove("FULL_LIST");
        entries.remove("STATUS");

        return entries.values().stream().map(v -> {
            try {
                return objectMapper.readValue((String) v, FlowDTO.class);
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
     * @param flowUserId - The owner of the flow
     * @param currentUserId - The current logged in user
     *                      This function checks if the current user is allowed to edit/delete/view the flow
     */
    public void isUserAllowed(String flowUserId, String currentUserId){
        if(!flowUserId.equals(currentUserId))
            throw new UserNotAllowedException("User is not allowed to access flow");
    }

    /**
     *
     * @param flowInput - Contains the flow name and the project ID
     * @return - Saves this flow with the flow name from the flowInput var and returns
     * flow DTO that contains all the new flow data
     */
    public FlowDTO createFlow(FlowInput flowInput, String userId) {
        Project project = projectService.findById(flowInput.getProjectId(), userId);


        Flow flow = aFlow()
                .withFlowName(flowInput.getFlowName())
                .withProjectId(project.getId())
                .withOwnerId(userId)
                .withSteps(new ArrayList<>())
                .withGlobalURL(flowInput.getGlobalURL())
                .withGlobalVariables(flowInput.getGlobalVariables())
                .withGlobalHeaders(flowInput.getGlobalHeaders())
                .withGlobalAssertions(new HashMap<>())
                .withLastModified(Instant.now())
                .build();

        FlowDTO newFlowDTO = flowRepository.save(flow).convertToDTO();
        updateCachedFlow(newFlowDTO);

        return newFlowDTO;
    }

    /**
     *
     * @param projectId - The ID of the project
     * @param userId - The current user ID
     * @return - All the flows of the project with projectId
     */
    public List<FlowDTO> getFlowsByProjectId(String projectId, String userId) {
        projectService.findById(projectId, userId);

        List<FlowDTO> flowDTOList = getProjectCachedFlows(projectId);

        if (flowDTOList != null) {
            return flowDTOList;
        }

        String redisKey = getRedisKey(projectId);
        Map<Object, Object> cachedFlowsMap = new HashMap<>();

        List<Flow> flowsList = flowRepository.findByProjectId(projectId);

        flowDTOList = flowsList.stream().map(flow -> {
            FlowDTO currentDTO = flow.convertToDTO();
            try {
                String stringFlowDTO = objectMapper.writeValueAsString(currentDTO);
                cachedFlowsMap.put(flow.getId(), stringFlowDTO);
            } catch (Exception e) {
                log.error("Failed to parse flow JSON for flowId: " + flow.getId(), e);
            }
            return currentDTO;
        }).collect(Collectors.toList());

        if (flowDTOList.isEmpty()) {
            redisTemplate.opsForHash().put(redisKey, "STATUS", "EMPTY");
            redisTemplate.opsForHash().delete(redisKey, "FULL_LIST");
        } else {
            redisTemplate.delete(redisKey);
            redisTemplate.opsForHash().putAll(redisKey, cachedFlowsMap);
            setFullListCachedStatus(projectId,  cachedFlowsMap.size() == flowsList.size());
        }

        updateExpire(redisKey);

        return flowDTOList;
    }

    /**
     *
     * @param flowId - The flow ID
     * @param userId - The user ID
     * @return - Returns the flow steps
     */
    public FlowStepsResponse getFlowSteps(String flowId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId, userId);

        FlowStepsResponse stepsResponse = new FlowStepsResponse(stepsProjection.getSteps(), stepsProjection.getFallbacks());
        return stepsResponse;
    }

    /**
     *
     * @param flowId - Flow ID
     * @param stepRequest - The step information (Including the step group -> fallbacks or steps)
     * @param userId - The user ID
     *  This function adds new step to the flow with the flowId
     */
    public String addStep(String flowId, StepRequest stepRequest, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId, userId);

        String stepUUID = UUID.randomUUID().toString();
        Step step = stepRequest.getStep();
        String fieldName = stepRequest.getStepGroup().toDbField();

        step.setId(stepUUID);

        Update update = new Update().push(fieldName, step);

        updateFlowInternals(update, stepsProjection.getId(), stepsProjection.getProjectId(), null);

        return stepUUID;
    }

    public void deleteFlow(String flowId, String userId){
        FlowOwnerIdProjection flowOwnerIdProjection = findOwnerIdProjectedById(flowId, userId);

        flowRepository.deleteById(flowId);

        String projectId =  flowOwnerIdProjection.getProjectId();
        String redisKey = getRedisKey(projectId);

        try {
            redisTemplate.opsForHash().delete(redisKey, flowId);

            if (!flowRepository.existsByProjectId(projectId)) {
                redisTemplate.opsForHash().put(redisKey, "STATUS", "EMPTY");
                redisTemplate.opsForHash().delete(redisKey, "FULL_LIST");
            } else {
                setFullListCachedStatus(projectId, false);
            }

            updateExpire(redisKey);

        } catch (Exception e) {
            log.error("Failed to update cache during deleteFlow for flowId: " + flowId, e);
            setFullListCachedStatus(projectId, false);
        }
    }

    /**
     * This function deletes all the flows of a project
     * @param projectId - The project ID
     */
    public void deleteFlowByProjectId(String projectId){
        flowRepository.deleteByProjectId(projectId);

        String redisKey = getRedisKey(projectId);
        try {
            redisTemplate.delete(redisKey);
            redisTemplate.opsForHash().put(redisKey, "STATUS", "EMPTY");
            updateExpire(redisKey);

        } catch (Exception e) {
            log.error("Failed to clear flows cache for projectId: " + projectId, e);
        }
    }

    /**
     * This function deletes step from flow
     * @param flowId - The ID of the flow
     * @param stepId - The ID of the Step
     * @param userId - The current user ID
     */
    public void deleteStep(String flowId, String stepId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId, userId);

        Update update = new Update().pull("steps", Query.query(Criteria.where("id").is(stepId)));

        updateFlowInternals(update, stepsProjection.getId(), stepsProjection.getProjectId(), null);

    }

    /**
     *
     * @param flowId - The flow ID
     * @param stepRequest - The updated step data
     * @param userId - The current user that logged in
     */
    public void editStep(String flowId, StepRequest stepRequest, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId, userId);

        String stepField = stepRequest.getStepGroup().toDbField();
        Step step = stepRequest.getStep();

        Criteria stepCriteria =  Criteria.where("id").is(flowId).and(stepField+".id").is(step.getId());
        Update update = new Update()
                .set(stepField+".$",  step);


        updateFlowInternals(update,
                stepsProjection.getId(),
                stepsProjection.getProjectId(),
                stepCriteria);
    }

    /**
     * This function edits an existing flow
     * @param flowEditInput - The flow edit input - contains the flowId, the new flow name and the new globalURL
     * @param userId - The ID of the current user
     */
    public void editFlow(FlowEditInput flowEditInput, String userId){
        findOwnerIdProjectedById(flowEditInput.getId(), userId);

        Query query = new Query(Criteria.where("id").is(flowEditInput.getId()));
        Update update = new Update()
                .set("flowName", flowEditInput.getFlowName())
                .set("globalURL", flowEditInput.getGlobalURL())
                .set("globalVariables", flowEditInput.getGlobalVariables())
                .set("globalHeaders", flowEditInput.getGlobalHeaders())
                .set("lastModified", Instant.now());

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        Flow flow = mongoTemplate.findAndModify(query, update, options, Flow.class);

        if(flow != null)
            updateCachedFlow(flow.convertToDTO());
    }

    /**
     *
     * @param setGlobalsRequest - contains the globals map and the field that these globals attached to
     * @param flowId - The flow ID
     * @param userId - The current user ID
     */
    public void setGlobals(SetGlobalsRequest setGlobalsRequest, String flowId, String userId){
        FlowOwnerIdProjection flowOwnerIdProjection = findOwnerIdProjectedById(flowId, userId);

        String fieldName = setGlobalsRequest.getFieldName().toDbName();

        Update update = new Update().set(fieldName, setGlobalsRequest.getGlobals());

        updateFlowGlobalsAndSyncCache(update, flowOwnerIdProjection.getId(), flowOwnerIdProjection.getProjectId(), null);
    }

    /**
     *
     * @param flowId - The flow ID
     * @param userId - The current user ID
     * @return - Projected the flow steps
     */
    private FlowStepsProjection findStepsProjectedById(String flowId, String userId) {
        FlowStepsProjection flowStepsProjection =  flowRepository.findStepsProjectedById(flowId)
                .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));

        isUserAllowed(flowStepsProjection.getOwnerId(), userId);

        return flowStepsProjection;
    }

    /**
     *
     * @param flowId - The flow ID
     * @param userId - The current user ID
     * @return - Flow data with owner id projected
     */
    public FlowOwnerIdProjection findOwnerIdProjectedById(String flowId, String userId) {
        FlowOwnerIdProjection flowOwnerIdProjection = flowRepository.findOwnerIdProjectedById(flowId)
                .orElseThrow(() -> new FlowNotFound("This flow is not exists!"));
        isUserAllowed(flowOwnerIdProjection.getOwnerId(), userId);

        return flowOwnerIdProjection;
    }

    /**
     *
     * @param flowId - The flow ID
     * @param fallbackId - The fallback ID which will be deleted
     * @param userId - The current user that logged in
     */
    public void deleteFallback(String flowId, String fallbackId, String userId){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId, userId);

        Query query = new Query(Criteria.where("id").is(flowId));

        Document condDoc = new Document("$ne", List.of("$$this.v", fallbackId));

        MongoExpression updateStepsExpression = () -> Document.parse(
                "{$map: {" +
                        "  input: '$steps'," +
                        "  as: 'step'," +
                        "  in: {$mergeObjects: [" +
                        "    '$$step'," +
                        "    {routes: {$arrayToObject: {$filter: {" +
                        "      input: {$objectToArray: '$$step.routes'}," +
                        "      cond: " + condDoc.toJson() + // <--- מוזרק כ-JSON מובנה ובטוח של מונגו
                        "    }}}}" +
                        "  ]}" +
                        "}}"
        );

        Instant newTime = Instant.now();

        Document filterDoc = new Document("$ne", List.of("$$fb._id", fallbackId));

        AggregationUpdate update = AggregationUpdate.update()
                .set("fallbacks").toValue((MongoExpression) () -> Document.parse(
                        "{$filter: { input: '$fallbacks', as: 'fb', cond: " + filterDoc.toJson() + " }}"
                ))
                .set("steps").toValue(updateStepsExpression)
                .set("lastModified").toValue(newTime);

        mongoTemplate.updateFirst(query, update, Flow.class);

        updateLastModifiedInCache(stepsProjection.getId(), stepsProjection.getProjectId(), newTime);

    }

    /**
     *
     * @param flowId -  The flow ID
     * @param userId - The current user ID
     * This function add a step between two steps
     */
    public void syncCanvasSteps(String flowId, String userId, SyncStepsRequest syncStepsRequest){
        FlowStepsProjection stepsProjection = findStepsProjectedById(flowId, userId);

        List<Step> steps = stepsProjection.getSteps();

        Map<String, Step> stepMap = steps.stream().collect(Collectors.toMap(Step::getId, step -> step));

        Step stepToAdd = syncStepsRequest.getStep();
        List<SyncStepsRequest.ReorderItem> reorderItemList = syncStepsRequest.getReorderSteps();

        List<Step> newOrderSteps = new ArrayList<>();
        boolean isNewStepAdded = false;

        for (SyncStepsRequest.ReorderItem reorderItem : reorderItemList) {
            String currentStepId = reorderItem.getId();
            Step currentStep = stepMap.remove(currentStepId);

            if (currentStep == null) {
                if (stepToAdd != null && !isNewStepAdded) {
                    stepToAdd.setId(currentStepId);
                    currentStep = stepToAdd;
                    isNewStepAdded = true;
                } else {
                    throw new BadRequestException("Invalid, duplicate, or unexpected Step ID: " + currentStepId);
                }
            }

            currentStep.setPosition(reorderItem.getPosition());
            newOrderSteps.add(currentStep);
        }

        if (!stepMap.isEmpty()) {
            throw new BadRequestException("Missing existing steps in request!");
        }

        Update update = new Update().set("steps", newOrderSteps);

        updateFlowInternals(update, stepsProjection.getId(), stepsProjection.getProjectId(), null);
    }

    /**
     * An help function to DRY code the mongo template update fields
     * @param update - The update query
     * @param flowId - The flow ID
     * @param projectId - The project ID for the redis key
     * @param additionalCriteria - Some additional criteria (Can be null)
     */
    public void updateFlowInternals(Update update, String flowId, String projectId, Criteria additionalCriteria){
        Criteria baseCriteria = Criteria.where("id").is(flowId);

        Query query = (additionalCriteria != null)
                ? new Query(new Criteria().andOperator(baseCriteria, additionalCriteria))
                : new Query(baseCriteria);

        Instant newTime = Instant.now();

        update.set("lastModified", newTime);


        mongoTemplate.updateFirst(query, update, Flow.class);
        updateLastModifiedInCache(flowId, projectId, newTime);
    }


    public void updateFlowGlobalsAndSyncCache(Update update, String flowId, String projectId, Criteria additionalCriteria){
        Criteria baseCriteria = Criteria.where("id").is(flowId);

        Query query = (additionalCriteria != null)
                ? new Query(new Criteria().andOperator(baseCriteria, additionalCriteria))
                : new Query(baseCriteria);

        Instant newTime = Instant.now();

        update.set("lastModified", newTime);

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        Flow flow = mongoTemplate.findAndModify(query, update, options, Flow.class);
        if(flow != null) {
            flow.setLastModified(newTime);
            updateCachedFlow(flow.convertToDTO());
        }

    }

    public Flow createMockupFlow(String projectId, String userId){
        Project project = projectService.findById(projectId, userId);

        String globalURL = "https://dummyjson.com";
        FlowBuilder flowBuilder = aFlow()
                .withFlowName("DEMO-FLOW")
                .withOwnerId(userId)
                .withProjectId(projectId)
                .withGlobalURL(globalURL)
                .withGlobalHeaders(Map.of("Content-Type", "application/json", "Authorization", "Bearer {{token}}"))
                .withLastModified(Instant.now())
                .withGlobalAssertions(new HashMap<>())
                .withGlobalVariables(Map.of("username", "emilys", "password", "emilyspass"))
                .withFallbacks(new ArrayList<>());

        Step loginStep = aStep()
                .withUrl(globalURL+"/auth/login")
                .withTitle("LOGIN-DEMO-STEP")
                .withId(UUID.randomUUID().toString())
                .withHttpMethod("POST")
                .withHeaders(Map.of())
                .withBody("{\"username\": \"{{username}}\", \"password\": \"{{password}}\"}")
                .withExtract(Map.of("jwtToken", "accessToken", "userId", "id"))
                .withAssertions(new HashMap<>())
                .withRoutes(new HashMap<>())
                .withPosition(new Step.Position(80, 80))
                .build();

        Step addPostStep = aStep()
                .withUrl(globalURL+"/posts/add")
                .withTitle("ADD-POST-DEMO-STEP")
                .withId(UUID.randomUUID().toString())
                .withHttpMethod("POST")
                .withHeaders(Map.of())
                .withBody("{\"title\": \"This is my first mock post!\", \"userId\": \"{{userId}}\"}")
                .withExtract(Map.of("postId", "id"))
                .withAssertions(new HashMap<>())
                .withRoutes(new HashMap<>())
                .withPosition(new Step.Position(520, 300))
                .build();

        Step getUserPosts = aStep()
                .withUrl(globalURL+"/users/{{userId}}/posts")
                .withTitle("GET-USER-POSTS-DEMO-STEP")
                .withId(UUID.randomUUID().toString())
                .withHttpMethod("GET")
                .withHeaders(new HashMap<>())
                .withBody("")
                .withExtract(new HashMap<>())
                .withAssertions(new HashMap<>())
                .withRoutes(new HashMap<>())
                .withPosition(new Step.Position(960, 80))
                .build();

        Flow newMockupFlow = flowBuilder.withSteps(List.of(loginStep, addPostStep, getUserPosts)).build();

        newMockupFlow = flowRepository.save(newMockupFlow);
        updateCachedFlow(newMockupFlow.convertToDTO());

        return newMockupFlow;
    }

    public enum StepGroup{
        FALLBACKS,
        STEPS;

        public String toDbField(){
            String field = "steps";
            switch (this) {
                case FALLBACKS:
                    field= "fallbacks";
                    break;
                case STEPS:
                    field = "steps";
                    break;
                default:
                    field = "steps";
                    break;
            }

            return field;
        }
    }
}
