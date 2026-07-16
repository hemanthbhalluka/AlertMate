package com.project.public_safety_app.service;

import com.project.public_safety_app.model.Quiz;
import com.project.public_safety_app.model.SelfAssessmentResult;
import com.project.public_safety_app.model.User;
import com.project.public_safety_app.repository.SelfAssessmentResultRepository;
import com.project.public_safety_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import java.util.Optional;

@Service
public class SelfAssessmentResultServiceImpl implements SelfAssessmentResultService {

    @Autowired
    private SelfAssessmentResultRepository selfAssessmentResultRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    private final PolicyFactory sanitizer = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

    public SelfAssessmentResult createResult(SelfAssessmentResult result, User user) {
        validateSelfAssessmentResult(result);

        Quiz quiz = result.getQuiz();
        Quiz sanitizedQuiz = sanitizeQuiz(quiz);
        result.setQuiz(sanitizedQuiz);

        // Sanitize comments before saving
        if (result.getComments() != null) {
            result.setComments(sanitizer.sanitize(result.getComments()));
        }

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

        // Additional validation for other fields can be added here
    }

    private Quiz sanitizeQuiz(Quiz quiz) {
        if (quiz == null) {
            return null;
        }
        Quiz sanitizedQuiz = new Quiz();
        sanitizedQuiz.setId(quiz.getId());
        sanitizedQuiz.setTitle(quiz.getTitle());
        sanitizedQuiz.setDescription(quiz.getDescription());
        sanitizedQuiz.setSensitiveData(null); // Assuming sensitiveData is a field in Quiz

        // Handle nested objects explicitly
        if (quiz.getNestedObject() != null) {
            sanitizedQuiz.setNestedObject(sanitizeNestedObject(quiz.getNestedObject()));
        }

        return sanitizedQuiz;
    }

    private NestedObject sanitizeNestedObject(NestedObject nestedObject) {
        if (nestedObject == null) {
            return null;
        }
        NestedObject sanitizedNestedObject = new NestedObject();
        sanitizedNestedObject.setField1(nestedObject.getField1());
        sanitizedNestedObject.setField2(null); // Assuming field2 is sensitive
        return sanitizedNestedObject;
    }

    public void deleteResult(Long id, User requestingUser) {
        Optional<SelfAssessmentResult> resultOptional = selfAssessmentResultRepository.findById(id);
        if (resultOptional.isEmpty()) {
            throw new IllegalArgumentException("Result not found");
        }

        SelfAssessmentResult result = resultOptional.get();
        if (!result.getUser().getId().equals(requestingUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this result");
        }

        selfAssessmentResultRepository.deleteById(id);
    }
}