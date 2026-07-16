package com.project.public_safety_app.service;

import com.project.public_safety_app.model.Quiz;
import com.project.public_safety_app.model.SelfAssessmentResult;
import com.project.public_safety_app.model.User;
import com.project.public_safety_app.repository.SelfAssessmentResultRepository;
import com.project.public_safety_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SelfAssessmentResultServiceImpl implements SelfAssessmentResultService{

    @Autowired
    private SelfAssessmentResultRepository selfAssessmentResultRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    public SelfAssessmentResult createResult(SelfAssessmentResult result, User user) {
        validateSelfAssessmentResult(result);

        Quiz quiz = result.getQuiz();
        Quiz sanitizedQuiz = sanitizeQuiz(quiz);
        result.setQuiz(sanitizedQuiz);

        return selfAssessmentResultRepository.save(result);
    }

    private void validateSelfAssessmentResult(SelfAssessmentResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Result cannot be null");
        }

        if (result.getScore() < 0 || result.getScore() > 100) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }

        if (result.getComments() != null && result.getComments().length() > 500) {
            throw new IllegalArgumentException("Comments cannot exceed 500 characters");
        }

        if (result.getQuiz() == null) {
            throw new IllegalArgumentException("Quiz cannot be null");
        }

        // Add more field-specific validations as needed
    }

    private Quiz sanitizeQuiz(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        Quiz sanitizedQuiz = new Quiz();
        sanitizedQuiz.setId(quiz.getId());
        sanitizedQuiz.setTitle(quiz.getTitle());
        sanitizedQuiz.setDescription(quiz.getDescription());
        // Exclude or mask sensitive fields
        sanitizedQuiz.setSensitiveData(null); // Assuming sensitiveData is a field in Quiz
        return sanitizedQuiz;
    }

    public void deleteResult(Long id) {
        selfAssessmentResultRepository.deleteById(id);
    }

}