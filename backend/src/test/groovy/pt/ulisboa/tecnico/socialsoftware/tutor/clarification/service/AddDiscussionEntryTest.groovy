package pt.ulisboa.tecnico.socialsoftware.tutor.clarification.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.Clarification
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.domain.DiscussionEntry
import pt.ulisboa.tecnico.socialsoftware.tutor.clarification.dto.DiscussionEntryDto
import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.course.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.DISCUSSION_ENTRY_TITLE_IS_EMPTY

@DataJpaTest
class AddDiscussionEntryTest extends SpockTest {
    def questionAnswer
    def user
    def quiz
    def quizAnswer
    def quizQuestion
    def course
    def courseExecution

    def setup() {
        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL)
        courseExecutionRepository.save(courseExecution)

        user = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, User.Role.STUDENT, true, false)
        user.addCourse(courseExecution)
        userRepository.save(user)
        user.setKey(user.getId())

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(courseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        def question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(course)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)

        questionAnswer = new QuestionAnswer()
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)

        def clarification = new Clarification()
        clarification.setQuestionAnswer(questionAnswer)
        clarification.setTitle(CLARIFICATION_1_TITLE)
        clarification.setId(CLARIFICATION_1_ID)
        clarification.setUser(user)
        clarificationRepository.save(clarification)
    }

    def 'DiscussionEntry well done'(){
        given: 'a DiscussionEntry'
        def discussionEntryDto = new DiscussionEntryDto();
        def clarification =  clarificationRepository.findAll().get(0)
        discussionEntryDto.setClarificationId(clarification.getId())
        discussionEntryDto.setId(DISCUSSION_ENTRY_1_ID)
        discussionEntryDto.setMessage(DISCUSSION_1_MESSAGE)
        def user = userRepository.findAll().get(0)
        discussionEntryDto.setUserId(user.getId())

        when:
        def dE = clarificationService.addDiscussionEntry(clarification.getId(), discussionEntryDto, false, userRepository.findAll().get(0).getId())

        then:
        def discEnt = clarificationRepository.findAll().get(0).getDiscussionEntries()[0]
        discEnt.getId() == DISCUSSION_ENTRY_1_ID
        discEnt.getMessage() == DISCUSSION_1_MESSAGE
        discEnt.getUser().getId() == user.getId()
        dE.getId() == DISCUSSION_ENTRY_1_ID
        dE.getMessage() == DISCUSSION_1_MESSAGE
        dE.getUserId() == user.getId()
    }

    def 'DiscussionEntry without a message'() {
        given: 'a DiscussionEntry'
        DiscussionEntry discussionEntry = new DiscussionEntry()
        discussionEntry.setId(DISCUSSION_ENTRY_1_ID)
        discussionEntry.setUser(userRepository.findAll().get(0))
        def clarification = clarificationRepository.findAll().get(0)
        discussionEntry.setClarification(clarification)

        when:
        clarificationService.addDiscussionEntry(clarification.getId(), new DiscussionEntryDto(discussionEntry), false, userRepository.findAll().get(0).getId())

        then:
        clarificationRepository.findAll().get(0).getDiscussionEntries().size() == 0
        TutorException exception = thrown()
        exception.getErrorMessage() == DISCUSSION_ENTRY_TITLE_IS_EMPTY
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}