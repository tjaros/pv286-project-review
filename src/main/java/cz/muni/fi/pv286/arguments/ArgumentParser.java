package cz.muni.fi.pv286.arguments;

import cz.muni.fi.pv286.arguments.values.FileType;
import cz.muni.fi.pv286.arguments.values.Format;
import cz.muni.fi.pv286.arguments.values.Option;

import java.util.*;

class ArgumentParser {

    static private final List<String> formats = new ArrayList<>(Arrays.asList("bytes", "hex", "int", "bits", "array"));
    static private final List<String> inputOptions = new ArrayList<>(Arrays.asList("big", "little", "left", "right"));
    static private final List<String> outputOptions = new ArrayList<>(Arrays.asList("big", "little", "left", "right",
            "0x", "0", "a", "{}", "}", "{", "()", "(", ")", "[]", "[", "]"));
    static private final Map<String, Format> formatMap;
    static private final Map<String, Option> optionMap;
    static {
        formatMap = new HashMap<>();
        formatMap.put("bytes", Format.BYTES);
        formatMap.put("hex", Format.HEX);
        formatMap.put("int", Format.INT);
        formatMap.put("bits", Format.BITS);
        formatMap.put("array", Format.ARRAY);
        optionMap = new HashMap<>();
        optionMap.put("big", Option.BIG);
        optionMap.put("little", Option.LITTLE);
        optionMap.put("left", Option.LEFT);
        optionMap.put("right", Option.RIGHT);
        optionMap.put("0x", Option.HEX);
        optionMap.put("0", Option.DEC);
        optionMap.put("a", Option.CHAR);
        optionMap.put("{}", Option.CURLY);
        optionMap.put("}", Option.CURLY);
        optionMap.put("{", Option.CURLY);
        optionMap.put("()", Option.REGULAR);
        optionMap.put("(", Option.REGULAR);
        optionMap.put(")", Option.REGULAR);
        optionMap.put("[]", Option.SQUARE);
        optionMap.put("[", Option.SQUARE);
        optionMap.put("]", Option.SQUARE);
    }

    public static void parseArguments(String[] from, ProgramArguments to) throws InvalidArgumentsException {
        List<Argument> arguments = splitArguments(from);
        convertArguments(to, arguments);
        setDefaultArguments(to);
    }

    private static List<Argument> splitArguments(String[] args) throws InvalidArgumentsException {
        int index = 0;
        List<Argument> arguments = new ArrayList<>();

        while(index < args.length) {
            String current = args[index];

            if (current.startsWith("--")) {
                if (current.equals("--help")) {
                    arguments.add(new Argument(current, ""));
                    index++;
                } else {
                    try {
                        String[] parts = current.split("=", 2);
                        arguments.add(new Argument(parts[0], parts[1]));
                        index += 1;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new InvalidArgumentsException("Invalid long option format");
                    }
                }
            } else if (current.startsWith("-")) {
                if (!current.equals("-h") && index + 1 < args.length) {
                    arguments.add(new Argument(current, args[index + 1]));
                    index += 2;
                } else if (current.equals("-h")) {
                    arguments.add(new Argument(current, ""));
                    index += 1;
                } else {
                    throw new InvalidArgumentsException("Invalid short option format");
                }
            } else {
                throw new InvalidArgumentsException("Invalid option format");
            }
        }

        return arguments;
    }

    private static void convertArguments(ProgramArguments result, List<Argument> arguments) throws InvalidArgumentsException {
        for (Argument argument : arguments) {
            switch (argument.getName()) {
                case "-f":
                case "--from":
                    if (!formats.contains(argument.getValue()))
                        throw new InvalidArgumentsException("Invalid value for input format");
                    result.setInputFormat(formatMap.get(argument.getValue()));
                    break;
                case "-t":
                case "--to":
                    if (!formats.contains(argument.getValue()))
                        throw new InvalidArgumentsException("Invalid value for output format");
                    result.setOutputFormat(formatMap.get(argument.getValue()));
                    break;
                case "--from-options":
                    if (!inputOptions.contains(argument.getValue()))
                        throw new InvalidArgumentsException("Invalid value for input options");
                    result.addInputOption(optionMap.get(argument.getValue()));
                    break;
                case "--to-options":
                    if (!outputOptions.contains(argument.getValue()))
                        throw new InvalidArgumentsException("Invalid value for output options");
                    result.addOutputOption(optionMap.get(argument.getValue()));
                    break;
                case "-i":
                case "--input":
                    result.setInputFileType(FileType.FILE);
                    result.setInputFileName(argument.getValue());
                    break;
                case "-o":
                case "--output":
                    result.setOutputFileType(FileType.FILE);
                    result.setOutputFileName(argument.getValue());
                    break;
                case "-d":
                case "--delimiter":
                    result.setDelimiter(argument.getValue());
                    break;
                case "-h":
                case "--help":
                    if (!argument.getValue().isEmpty())
                        throw new InvalidArgumentsException("Help option cannot take value");
                    if (arguments.size() > 1)
                        throw new InvalidArgumentsException("Help option be called with other options");
                    result.setHelp(true);
                    break;
                default:
                    /* Unknown options */
                    throw new InvalidArgumentsException("Unknown option");
            }
        }
    }

    private static void setDefaultArguments(ProgramArguments arguments) throws InvalidArgumentsException {
        switch (arguments.getInputFormat()) {
            case NONE:
            case BYTES:
            case HEX:
                break;
            case INT:
                if (arguments.getInputOption().isEmpty()) {
                    arguments.addInputOption(Option.BIG);
                }
                if (arguments.getOutputOption().isEmpty()) {
                    arguments.addOutputOption(Option.BIG);
                }
                break;
            case BITS:
                if (arguments.getInputOption().isEmpty()) {
                    arguments.addInputOption(Option.LEFT);
                }
                break;
            case ARRAY:
                /* Prepend default values, if overwritten, the latter is taken */
                arguments.getOutputOption().add(0, Option.HEX);
                arguments.getOutputOption().add(0, Option.CURLY);
                break;
            default:
                throw new InvalidArgumentsException("Undefined input format");
        }
    }
}
