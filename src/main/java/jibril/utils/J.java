package jibril.utils;

import kotlin.Pair;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.List;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;

/**
 * Bypassing most Kotlin (and Java) memes.
 */
public class J {

    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        if (name.length() > 1 && isUpperCase(name.charAt(1)) && isUpperCase(name.charAt(0))) {
            //IO -> io; JDA -> jda
            if (name.equals(name.toUpperCase())) return name.toLowerCase();
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = toLowerCase(chars[0]);
        return new String(chars);
    }

    public static String exceptionName(Exception e) {
        Class<?> c = e.getClass();

        while (c != null) {
            String name = c.getSimpleName();
            if (!name.isEmpty()) return name;
            c = c.getSuperclass();
        }

        return null;
    }

    public static String exceptionType(Exception e) {
        Class<?> c = e.getClass();

        while (c != null && !c.equals(Exception.class)) {

            String name = c.getSimpleName();
            if (!name.isEmpty()) {
                if (name.endsWith("Exception")) return name.substring(0, name.length() - 9);
                if (name.endsWith("Error")) return name.substring(0, name.length() - 5);
                return name;
            }
            c = c.getSuperclass();
        }

        return "Exception";
    }

    public static String initials(String s) {
        return s.codePoints().filter(Character::isUpperCase).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    public static Pair<String, String> split(String s) {
        List<String> split = StringsKt.split(s, new char[]{' '}, false, 2);
        return new Pair<>(split.get(0), split.get(1));
    }

    /**
     * toString + array "unboxing" + builder reusing
     *
     * @param any Any object, from null to an array
     * @return if the object is an array, an actual array representation, else a toString()
     */
    @NotNull
    public static String toString(@Nullable Object any) {
        if (any == null) {
            return "null";
        } else if (!any.getClass().isArray()) {
            return any.toString();
        }

        StringBuilder builder = new StringBuilder();
        toString(any, builder);
        return builder.toString();
    }

    private static void toString(Object any, StringBuilder builder) {
        if (any == null) {
            builder.append("null");
            return;
        }

        if (!any.getClass().isArray()) {
            builder.append(any);
            return;
        }

        int length = Array.getLength(any);
        if (length == 0) {
            builder.append("[]");
            return;
        }

        for (int i = 0; i < length; i++) {
            builder.append(i == 0 ? "[" : ", ");
            toString(Array.get(any, i), builder);
        }

        builder.append("]");
    }
}
