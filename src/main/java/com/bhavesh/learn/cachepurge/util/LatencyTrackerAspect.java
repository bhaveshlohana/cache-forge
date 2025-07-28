//package com.bhavesh.learn.cachepurge.util;
//
//import com.bhavesh.learn.cachepurge.annotation.TrackLatency;
//import com.bhavesh.learn.cachepurge.model.Cache; // Import the Cache interface
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Aspect
//@Component
//public class LatencyTrackerAspect {
//
//    // LatencyStatsCollector is still available for detailed per-method stats if needed,
//    // but total latency is now tracked by the Cache itself.
//    @Autowired
//    private LatencyStatsCollector collector;
//
//    @Around("@annotation(com.bhavesh.learn.cachepurge.annotation.TrackLatency)")
//    public Object measureLatency(ProceedingJoinPoint joinPoint) throws Throwable {
//        long start = System.nanoTime();
//        Object result = joinPoint.proceed(); // Execute the original method
//        long duration = System.nanoTime() - start; // Calculate duration
//
//        // Get the method signature to retrieve the annotation value (if any)
//        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
//        TrackLatency trackLatency = signature.getMethod().getAnnotation(TrackLatency.class);
//
//        // Determine the label for LatencyStatsCollector (optional)
//        String methodSignatureString = signature.toShortString();
//        String tag = trackLatency != null ? trackLatency.value() : "";
//        String label = tag.isEmpty() ? methodSignatureString : methodSignatureString + " (" + tag + ")";
//
//        // Record stats in LatencyStatsCollector (per method)
//        collector.record(label, duration);
//
//        // CRUCIAL: Pass the measured latency to the Cache instance itself
//        // The 'this' object of the join point is the actual cache instance being advised
//        Object target = joinPoint.getThis();
//        if (target instanceof Cache) { // Ensure it's an instance of our Cache interface
//            ((Cache<?, ?>) target).addLatency(duration); // Call the new addLatency method
//        } else {
//            System.err.println("LatencyTrackerAspect: Annotated method not on a Cache instance. " + target.getClass().getName());
//        }
//
//        return result;
//    }
//}