package games.fatboychummy.cc_tmp.client.scanner.docs;

import java.util.ArrayList;

public record PeripheralDocMethod(
        String name,
        String description,
        String shortDescription,
        boolean mainThread,
        ArrayList<PeripheralDocParameter> arguments,
        ArrayList<PeripheralDocParameter> returnValues
) {}
