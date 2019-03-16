package io.github.splotycode.mosaik.util.condition;

import io.github.splotycode.mosaik.annotations.Disabled;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassConditions {

    public static final Predicate<Class> NEED_ABSTRACT = item -> Modifier.isAbstract(item.getModifiers());
    public static final Predicate<Class> NOT_ABSTRACT = Conditions.reverse(NEED_ABSTRACT);

    public static final Predicate<AnnotatedElement> NO_DISABLE_ANNOTATION = unallowedAnnotation(Disabled.class);

    public static final Predicate<Class> MUST_BE_CLASS = item -> !item.isEnum() && !item.isInterface();

    public static <C extends Class> Predicate<C> mustBeClass() {
        return (Predicate<C>) MUST_BE_CLASS;
    }

    public static Predicate<Class> isInPackage(String packageName) {
        return clazz -> clazz.getName().startsWith(packageName);
    }

    public static Predicate<Class> instanceOf(Class clazz) {
        return clazz::isInstance;
    }

    public static Predicate<Class> assignable(Class<?> clazz) {
        return clazz::isAssignableFrom;
    }

    public static <S> Predicate<S> assignableClass(Class<? extends S> clazz) {
        return s -> clazz.isAssignableFrom(s.getClass());
    }

    public static Predicate<AnnotatedElement> needAnnotation(Class<? extends Annotation> annotation) {
        return item -> item.isAnnotationPresent(annotation);
    }

    public static Predicate<AnnotatedElement> needOneAnnotation(Class<? extends Annotation>... annotations) {
        return item -> Arrays.stream(annotations).anyMatch(item::isAnnotationPresent);
    }

    public static Predicate<AnnotatedElement> needOneAnnotation(Collection<? extends Class<? extends Annotation>> annotations) {
        return item -> annotations.stream().anyMatch(item::isAnnotationPresent);
    }

    public static Predicate<AnnotatedElement> needAnnotations(Class<? extends Annotation>... annotations) {
        return item -> Arrays.stream(annotations).allMatch(item::isAnnotationPresent);
    }

    public static Predicate<AnnotatedElement> needAnnotations(Collection<? extends Class<? extends Annotation>> annotations) {
        return item -> annotations.stream().allMatch(item::isAnnotationPresent);
    }

    public static Predicate<AnnotatedElement> unallowedAnnotation(Class<? extends Annotation> annotation) {
        return item -> !item.isAnnotationPresent(annotation);
    }

    public static Predicate<Class> anyMethod(Predicate<Method> methodCondition) {
        return item -> Arrays.stream(item.getDeclaredMethods()).anyMatch(methodCondition::test);
    }

    public static Predicate<Class> allMethods(Predicate<Method> methodCondition) {
        return item -> Arrays.stream(item.getDeclaredMethods()).anyMatch(methodCondition::test);
    }


}
