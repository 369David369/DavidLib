package io.github.splotycode.mosaik.argparserimpl;

import io.github.splotycode.mosaik.argparser.IArgParser;
import io.github.splotycode.mosaik.runtime.LinkBase;
import io.github.splotycode.mosaik.util.collection.CollectionUtil;
import io.github.splotycode.mosaik.util.reflection.ReflectionUtil;
import io.github.splotycode.mosaik.valuetransformer.TransformerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Arg Parser
 */
public class ArgParser implements IArgParser {

    private Map<String[], ParsedArguments> cachedArguments = new HashMap<>();
    private Map<Object, ParsedObject> cachedObjects = new HashMap<>();

    @Override
    public void parseArgs(Object obj, String label, String[] args) {
        ParsedArguments arguments = getArguments(args);
        ParsedObject object = cachedObjects.get(obj);

        if (object == null) {
            object = ParsedObject.parse(obj);
            cachedObjects.put(obj, object);
        }

        for (Argument argument : object.getAll()) {
            Class type = argument.getField().getType();
            String name = (label == null ? "" : label + ":") + argument.getName();
            String rawValue = arguments.getByKey(name);
            Object result;
            if (rawValue == null || rawValue.equals("_no_value_")) {
                if (!ReflectionUtil.isAssignable(Boolean.class, type)) {
                    if (argument.getParameter().needed()) {
                        throw new ArgParseException("Could not fill argument " + name + " because it does not exists in args");
                    }
                    continue;
                } else {
                    result = rawValue != null;
                }
            } else {
                result = TransformerManager.getInstance().transform(rawValue, argument.getField().getType());
            }
            try {
                argument.getField().set(obj, result);
            } catch (IllegalAccessException e) {
                throw new ArgParseException("Could not access " + obj.getClass().getName() + "#" + argument.getField().getName(), e);
            } catch (IllegalArgumentException e) {
                throw new ArgParseException("Type mismatch " + obj.getClass().getName() + "#" + argument.getField().getName(), e);
            }
        }
    }

    private ParsedArguments getArguments(String[] args) {
        ParsedArguments arguments = cachedArguments.get(args);
        if (arguments == null) {
            arguments = ParsedArguments.parse(args);
            cachedArguments.put(args, arguments);
        }
        return arguments;
    }

    @Override
    public Map<String, String> getParameters(String[] args) {
        return CollectionUtil.copy(getArguments(args).getArgumentMap());
    }

    @Override
    public Map<String, String> getParameters(String label, String[] args) {
        Map<String, String> parameters = new HashMap<>();
        Map<String, String> rawParameters = getArguments(args).getArgumentMap();

        for (String parameter : rawParameters.keySet()) {
            if (parameter.startsWith(label + ":")) {
                parameters.put(parameter.substring(label.length() + 1), rawParameters.get(parameter));
            }
        }

        return parameters;
    }

    @Override
    public Map<String, String> getParameters() {
        return getParameters(LinkBase.getBootContext().getArgs());
    }

    @Override
    public Map<String, String> getParameters(String label) {
        return getParameters(label, LinkBase.getBootContext().getArgs());
    }


    @Override
    public void parseArgs(Object obj) {
        parseArgs(obj, LinkBase.getBootContext().getArgs());
    }

    @Override
    public void parseArgs(Object obj, String label) {
        parseArgs(obj, label, LinkBase.getBootContext().getArgs());
    }

    @Override
    public void parseArgs(Object obj, String[] args) {
        parseArgs(obj, null, args);
    }

}
