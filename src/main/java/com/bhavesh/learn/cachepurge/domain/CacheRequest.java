package com.bhavesh.learn.cachepurge.domain;

import com.bhavesh.learn.cachepurge.domain.enums.OperationType;

public record CacheRequest(OperationType operationType, Integer key, String value) {

}
