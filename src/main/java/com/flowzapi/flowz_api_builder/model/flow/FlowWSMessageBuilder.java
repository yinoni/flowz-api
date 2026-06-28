package com.flowzapi.flowz_api_builder.model.flow;

public final class FlowWSMessageBuilder {
    private String status;
    private boolean success;
    private String message;

    private FlowWSMessageBuilder() {
    }

    public static FlowWSMessageBuilder aFlowWSMessage() {
        return new FlowWSMessageBuilder();
    }

    public FlowWSMessageBuilder withStatus(String status) {
        this.status = status;
        return this;
    }

    public FlowWSMessageBuilder withSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public FlowWSMessageBuilder withMessage(String message) {
        this.message = message;
        return this;
    }

    public FlowWSMessage build() {
        FlowWSMessage flowWSMessage = new FlowWSMessage();
        flowWSMessage.setStatus(status);
        flowWSMessage.setSuccess(success);
        flowWSMessage.setMessage(message);
        return flowWSMessage;
    }
}
