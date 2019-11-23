package de.jzjh.mensabuddy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quickbirdstudios.surveykit.*
import com.quickbirdstudios.surveykit.result.QuestionResult
import com.quickbirdstudios.surveykit.result.TaskResult
import com.quickbirdstudios.surveykit.result.question_results.SingleChoiceQuestionResult
import com.quickbirdstudios.surveykit.steps.CompletionStep
import com.quickbirdstudios.surveykit.steps.InstructionStep
import com.quickbirdstudios.surveykit.steps.QuestionStep
import com.quickbirdstudios.surveykit.survey.SurveyView
import de.jzjh.mensabuddy.models.UserRecord
import kotlinx.android.synthetic.main.activity_questions.*
import java.util.*
import kotlin.collections.HashMap

class QuestionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        val questions = listOf(
            InstructionStep(
                title = R.string.intro,
                text = R.string.intro_text,
                buttonText = R.string.intro_button
            ),
            QuestionStep(
                title = R.string.q1,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q1a1),
                        TextChoice(R.string.q1a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q2,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q2a1),
                        TextChoice(R.string.q2a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q3,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q3a1),
                        TextChoice(R.string.q3a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q4,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q4a1),
                        TextChoice(R.string.q4a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q5,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q5a1),
                        TextChoice(R.string.q5a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q6,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q6a1),
                        TextChoice(R.string.q6a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q7,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q7a1),
                        TextChoice(R.string.q7a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q8,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q8a1),
                        TextChoice(R.string.q8a2),
                        TextChoice(R.string.q8a3)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q9,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q9a1),
                        TextChoice(R.string.q9a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q10,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q10a1),
                        TextChoice(R.string.q10a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q11,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q11a1),
                        TextChoice(R.string.q11a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q12,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q12a1),
                        TextChoice(R.string.q12a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q13,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q13a1),
                        TextChoice(R.string.q13a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q14,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q14a1),
                        TextChoice(R.string.q14a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q15,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q15a1),
                        TextChoice(R.string.q15a2)
                    )
                )
            ),

            QuestionStep(
                title = R.string.q16,
                text = R.string.empty,
                answerFormat = AnswerFormat.SingleChoiceAnswerFormat(
                    textChoices = listOf(
                        TextChoice(R.string.q16a1),
                        TextChoice(R.string.q16a2),
                        TextChoice(R.string.q16a3),
                        TextChoice(R.string.q16a4)
                    )
                )
            ),

            CompletionStep(
                title = R.string.completion,
                text = R.string.completion_text,
                buttonText = R.string.completion_button
            )
        )

        val task = OrderedTask(steps = questions)

        val configuration = SurveyTheme(
            themeColorDark = ContextCompat.getColor(this, R.color.colorPrimaryDark),
            themeColor = ContextCompat.getColor(this, R.color.colorPrimary),
            textColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        )

        val answersMap = HashMap<String, Int>()
        val questionsFiltered: List<QuestionStep> = questions.filterIsInstance<QuestionStep>()
        questions_view.onSurveyFinish = { taskResult: TaskResult, reason: FinishReason ->
            if (reason == FinishReason.Completed) {
                val resultsFiltered = taskResult.results.filter { x -> x.results[0] is SingleChoiceQuestionResult}
                for(i in resultsFiltered.indices) {
                    val selectedAnswerValue = (resultsFiltered[i].results[0] as SingleChoiceQuestionResult).answer!!.value;
                    val index = (questionsFiltered[i].answerFormat as AnswerFormat.SingleChoiceAnswerFormat).textChoices
                        .indexOfFirst { x -> x.value == selectedAnswerValue }
                    answersMap.put(i.toString(), index)
                }
                saveQuestions(answersMap)
            }
        }

        questions_view.start(task, configuration)
    }

    fun saveQuestions(answersMap: Map<String, Int>) {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        db.collection("users")
            .document(user!!.uid)
            .set(UserRecord(user!!.uid, Date(), answersMap))
            .addOnSuccessListener { documentReference ->
            }
            .addOnFailureListener { e ->
            }
    }

    override fun onBackPressed() {
        return
    }
}
