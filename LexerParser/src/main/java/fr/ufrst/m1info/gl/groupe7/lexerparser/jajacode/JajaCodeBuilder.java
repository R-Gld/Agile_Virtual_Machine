package fr.ufrst.m1info.gl.groupe7.lexerparser.jajacode;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JajaCodeBuilder {

    private int adresseCounter;
    private final HashMap<Integer, String> instructions;

    public JajaCodeBuilder() {
        adresseCounter = 1;
        instructions = new HashMap<>();
    }

    /**
     * Append an instruction to the list of instructions.
     * \oplus_D
     * @param instr the instruction to append
     * @param args the params to pass to the instruction
     */
    public void addInstruction(JajaCodeInstr instr, Object... args) {
        addInstruction(JajaCodeInstructionBuilder.build(instr, args));
    }

    public void addInstruction(String ligne) {
        instructions.put(adresseCounter++, ligne);
    }

    /**
     * Prepend an instruction to the list of instructions.
     * \oplus_G
     * @param instr the instruction to prepend
     * @param args the params to pass to the instruction
     */
    public void prependInstruction(JajaCodeInstr instr, Object... args) {
        instructions.put(0, JajaCodeInstructionBuilder.build(instr, args));
        reorganiseInstructions();
    }

    /**
     * Fix the index of the hashMap {@code instructions}.
     * This hashmap shouldn't have index < 0
     */
    private void reorganiseInstructions() {
        HashMap<Integer, String> newInstructions = new HashMap<>();
        int counter = 1;
        for (int i = 0; i < instructions.size(); i++) {
            String instr = instructions.get(i);
            if (instr != null) {
                newInstructions.put(counter++, instr);
            }
        }
        instructions.clear();
        instructions.putAll(newInstructions);
        adresseCounter = instructions.size() + 1;
    }

    /**
     * Merge another JajaCodeBuilder into this one.
     * \oplus
     * @param other the other JajaCodeBuilder to merge
     */
    public void merge(JajaCodeBuilder other) {
        other.instructions.forEach((ignored, value) -> addInstruction(value));
    }

    /**
     * Get the list of instructions.
     * @return the list of instructions
     */
    public List<String> getInstructionsAsList() {
        return instructions.values().stream().toList();
    }

    @Override
    public String toString() {
        if (instructions.isEmpty()) return "";
        return String.join("\n", getInstructionsAsList()) + "\n";
    }

    public String toStringForInterpreter() {
        if (instructions.isEmpty()) { return ""; }
        return instructions.entrySet().stream().map(
                e -> e.getKey() + " " + e.getValue()
        ).collect(Collectors.joining("\n")) + "\n";
    }
}
