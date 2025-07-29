package com.bhavesh.learn.cacheforge.domain;

import com.bhavesh.learn.cacheforge.domain.enums.OperationType;

public record CacheRequest(OperationType operationType, Integer key, String value) {

}
