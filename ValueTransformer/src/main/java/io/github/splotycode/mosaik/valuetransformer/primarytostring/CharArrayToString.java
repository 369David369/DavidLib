package io.github.splotycode.mosaik.valuetransformer.primarytostring;

import io.github.splotycode.mosaik.util.ValueTransformer;
import io.github.splotycode.mosaik.util.datafactory.DataFactory;
import io.github.splotycode.mosaik.valuetransformer.TransformException;

public class CharArrayToString extends ValueTransformer<char[], String> {

    @Override
    public String transform(char[] input, DataFactory info) throws TransformException {
        return new String(input);
    }

}
