package com.shinetech.haloworld.hal.solver;

import com.shinetech.haloworld.hal.TextData;
import com.shinetech.haloworld.hal.Answer;
import com.shinetech.haloworld.hal.AnswerContext;
import com.shinetech.haloworld.hal.QuestionSolver;

import java.util.Date;

import static com.shinetech.haloworld.hal.Answer.TYPE.SIMPLE_TEXT_RESULT;

/**
 * Answers the question "What is the current time" or "what is the time"
 */
public class CurrentTimeQuestionSolver implements QuestionSolver {

    String[] questions = new String[] {
            "what time is it",
            "what is the time",
            "what is the current time",
            "it is what time"
    };

    @Override
    public void provideAnswer(String questionText, AnswerContext context) {
        TextData data = new TextData(new Date().toString());
        Answer answer = new Answer(SIMPLE_TEXT_RESULT.name(), data);
        context.publishAnswer(answer);
    }

    @Override
    public boolean canProvideAnswer(String questionText) {
        for(String question : questions) {
            if(questionText.contains(question)) {
                return true;
            }
        }
        return false;
    }

}
