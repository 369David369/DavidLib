package de.splotycode.davidlib.domparsing.keyvalue;

import me.david.davidlib.runtimeapi.parsing.DomParser;
import me.david.davidlib.runtimeapi.parsing.ParsingHandle;
import me.david.davidlib.runtimeapi.parsing.input.DomInput;

public class KeyValueHandle implements ParsingHandle {
    @Override
    public String[] getFileTypes() {
        return new String[] {"kv"};
    }

    @Override
    public String[] getMimeTypes() {
        return new String[] {"text/kv"};
    }

    @Override
    public DomParser getParser(DomInput input) {
        return new KeyValueParser();
    }
}
