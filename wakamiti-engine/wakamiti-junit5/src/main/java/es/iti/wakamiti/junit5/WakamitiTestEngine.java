/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.junit5;


import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.ModuleSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.function.Predicate;


/**
 * JUnit Platform {@link TestEngine} that discovers and executes Wakamiti test
 * plans (concrete classes annotated with {@link WakamitiPlan}).
 *
 * <p>By implementing a dedicated engine instead of relying on Jupiter dynamic
 * tests, every node of the plan exposes a real {@code TestSource} pointing to
 * the originating feature file, which enables IDE "jump to source" navigation
 * directly to the failing scenario or step.</p>
 */
public class WakamitiTestEngine implements TestEngine {

    static final String ENGINE_ID = "wakamiti";

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public Optional<String> getGroupId() {
        return Optional.of("es.iti.wakamiti");
    }

    @Override
    public Optional<String> getArtifactId() {
        return Optional.of("wakamiti-junit5");
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest request, UniqueId uniqueId) {
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "Wakamiti");

        request.getSelectorsByType(ClassSelector.class).forEach(selector ->
                addClass(engineDescriptor, selector.getJavaClass(), this::isPlanClass));

        request.getSelectorsByType(ClasspathRootSelector.class).forEach(selector ->
                ReflectionSupport.findAllClassesInClasspathRoot(
                        selector.getClasspathRoot(), this::isTopLevelPlanClass, name -> true
                ).forEach(candidate -> addClass(engineDescriptor, candidate, this::isTopLevelPlanClass)));

        request.getSelectorsByType(PackageSelector.class).forEach(selector ->
                ReflectionSupport.findAllClassesInPackage(
                        selector.getPackageName(), this::isTopLevelPlanClass, name -> true
                ).forEach(candidate -> addClass(engineDescriptor, candidate, this::isTopLevelPlanClass)));

        request.getSelectorsByType(ModuleSelector.class).forEach(selector ->
                ReflectionSupport.findAllClassesInModule(
                        selector.getModuleName(), this::isTopLevelPlanClass, name -> true
                ).forEach(candidate -> addClass(engineDescriptor, candidate, this::isTopLevelPlanClass)));

        request.getSelectorsByType(UniqueIdSelector.class).forEach(selector ->
                addByUniqueId(engineDescriptor, selector.getUniqueId()));

        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest request) {
        EngineExecutionListener listener = request.getEngineExecutionListener();
        TestDescriptor root = request.getRootTestDescriptor();
        listener.executionStarted(root);
        root.getChildren().stream()
                .filter(WakamitiClassDescriptor.class::isInstance)
                .map(WakamitiClassDescriptor.class::cast)
                .forEach(classDescriptor -> classDescriptor.execute(listener));
        listener.executionFinished(root, TestExecutionResult.successful());
    }

    private void addClass(
            EngineDescriptor engineDescriptor,
            Class<?> candidate,
            Predicate<Class<?>> predicate
    ) {
        if (!predicate.test(candidate)) {
            return;
        }
        UniqueId classId = engineDescriptor.getUniqueId().append("class", candidate.getName());
        if (engineDescriptor.findByUniqueId(classId).isPresent()) {
            return;
        }
        engineDescriptor.addChild(new WakamitiClassDescriptor(classId, candidate));
    }

    private void addByUniqueId(EngineDescriptor engineDescriptor, UniqueId uniqueId) {
        uniqueId.getSegments().stream()
                .filter(segment -> "class".equals(segment.getType()))
                .findFirst()
                .ifPresent(segment -> {
                    try {
                        addClass(engineDescriptor, Class.forName(segment.getValue()), this::isPlanClass);
                    } catch (ClassNotFoundException ignored) {
                        // The selected class is not available; nothing to discover.
                    }
                });
    }

    private boolean isPlanClass(Class<?> candidate) {
        return candidate != null
                && !Modifier.isAbstract(candidate.getModifiers())
                && !candidate.isInterface()
                && candidate.isAnnotationPresent(WakamitiPlan.class);
    }

    private boolean isTopLevelPlanClass(Class<?> candidate) {
        return isPlanClass(candidate)
                && !candidate.isMemberClass()
                && !candidate.isLocalClass()
                && !candidate.isAnonymousClass();
    }

}
