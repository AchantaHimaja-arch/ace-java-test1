

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class ArithmeticFlow_JavaCompute extends MbJavaComputeNode {

    @Override
    public void evaluate(MbMessageAssembly inAssembly) throws MbException {
        MbOutputTerminal out = getOutputTerminal("out");

        // Copy message
        MbMessage inMessage = inAssembly.getMessage();
        MbMessage outMessage = new MbMessage(inMessage);
        MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly, outMessage);

        try {
            // Navigate to JSON Data element
            MbElement jsonRoot = inMessage.getRootElement()
                    .getFirstElementByPath("JSON/Data");

            if (jsonRoot == null) {
                throw new MbUserException(this, "evaluate", "", "", "Invalid JSON structure", null);
            }

            // Extract values
            int a = Integer.parseInt(jsonRoot.getFirstElementByPath("a").getValueAsString());
            int b = Integer.parseInt(jsonRoot.getFirstElementByPath("b").getValueAsString());
            String op = jsonRoot.getFirstElementByPath("op").getValueAsString();

            double result;
            switch (op.toLowerCase()) {
                case "add":
                    result = a + b;
                    break;
                case "subtract":
                    result = a - b;
                    break;
                case "multiply":
                    result = a * b;
                    break;
                case "divide":
                    result = (b != 0) ? (double) a / b : Double.NaN;
                    break;
                default:
                    throw new MbUserException(this, "evaluate", "", "", "Unknown operation: " + op, null);
            }

            // Create new JSON for output
            MbElement outJsonRoot = outMessage.getRootElement()
                    .createElementAsLastChild(MbJSON.PARSER_NAME);
            MbElement outData = outJsonRoot.createElementAsLastChild(MbElement.TYPE_NAME, "Data", null);

            outData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "a", a);
            outData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "b", b);
            outData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "operation", op);
            outData.createElementAsLastChild(MbElement.TYPE_NAME_VALUE, "result", result);

        } catch (Exception e) {
            throw new MbUserException(this, "evaluate", "", "", e.toString(), null);
        }

        out.propagate(outAssembly);
    }
}
