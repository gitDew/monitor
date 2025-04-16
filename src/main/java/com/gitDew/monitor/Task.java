package com.gitDew.monitor;

import java.util.Map;

public record Task(DomainUser user, TaskType taskType, Map<String, String> params) {

}
