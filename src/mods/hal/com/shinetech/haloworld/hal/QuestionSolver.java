package com.shinetech.haloworld.hal;

/**
 * Implemented by objects that can answer specific questions parsed from a chat message.
 */
public interface QuestionSolver {

    /**
     * Triggers the question solver to provide an initial answer and optionally send regular updates if it makes sense.
     * @param context a context object that can be used to provide the answers asynchronously.
     * @param questionText The text containing the question. The text will always be lowercased.
     */
    public void provideAnswer(String questionText, AnswerContext context);

    /**
     * Returns <code>true</code> if it can provide answer to any question contained in the given text. The text
     * is always lowercased.
     */
    public boolean canProvideAnswer(String questionText);
}
