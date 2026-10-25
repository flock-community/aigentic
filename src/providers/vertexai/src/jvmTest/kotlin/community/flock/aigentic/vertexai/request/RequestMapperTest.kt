package community.flock.aigentic.vertexai.request

import com.google.genai.types.PartMediaResolutionLevel
import community.flock.aigentic.core.message.Message
import community.flock.aigentic.core.message.MessageCategory
import community.flock.aigentic.core.message.MimeType
import community.flock.aigentic.core.message.Sender
import community.flock.aigentic.core.model.GenerationSettings
import community.flock.aigentic.core.model.ThinkingConfig
import community.flock.aigentic.core.model.ThinkingLevel
import community.flock.aigentic.core.tool.Parameter
import community.flock.aigentic.core.tool.ParameterType
import community.flock.aigentic.core.tool.PrimitiveValue
import community.flock.aigentic.core.tool.ToolDescription
import community.flock.aigentic.core.tool.ToolName
import community.flock.aigentic.vertexai.VertexAIMediaResolution
import community.flock.aigentic.vertexai.VertexAIModelIdentifier
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.serialization.json.Json
import java.util.Optional

class RequestMapperTest :
    DescribeSpec({

        describe("VertexAI Request Mapper") {

            it("should set the media resolution on every media part when configured") {
                val messages =
                    listOf(
                        Message.Base64(Sender.Agent, "iVBORw0KGgoAAA==", MimeType.PNG, MessageCategory.EXECUTION),
                        Message.Url(Sender.Agent, "gs://bucket/scan.pdf", MimeType.PDF, MessageCategory.EXECUTION),
                    )

                createRequestContents(messages, VertexAIMediaResolution.ULTRA_HIGH)
                    .flatMap { it.parts().get() }
                    .map {
                        it
                            .mediaResolution()
                            .get()
                            .level()
                            .get()
                            .knownEnum()
                    } shouldBe List(2) { PartMediaResolutionLevel.Known.MEDIA_RESOLUTION_ULTRA_HIGH }
            }

            it("should serialize every media resolution as its Gemini level") {
                val base64Message = Message.Base64(Sender.Agent, "iVBORw0KGgoAAA==", MimeType.PNG, MessageCategory.EXECUTION)

                mapOf(
                    VertexAIMediaResolution.LOW to "MEDIA_RESOLUTION_LOW",
                    VertexAIMediaResolution.MEDIUM to "MEDIA_RESOLUTION_MEDIUM",
                    VertexAIMediaResolution.HIGH to "MEDIA_RESOLUTION_HIGH",
                    VertexAIMediaResolution.ULTRA_HIGH to "MEDIA_RESOLUTION_ULTRA_HIGH",
                ).forEach { (resolution, level) ->
                    createRequestContents(listOf(base64Message), resolution)
                        .single()
                        .toJson() shouldContain "\"mediaResolution\":{\"level\":\"$level\"}"
                }
            }

            it("should not set a media resolution when not configured") {
                val base64Message = Message.Base64(Sender.Agent, "iVBORw0KGgoAAA==", MimeType.PNG, MessageCategory.EXECUTION)

                createRequestContents(listOf(base64Message))
                    .single()
                    .toJson() shouldNotContain "mediaResolution"
            }

            it("should set max output tokens when configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(maxOutputTokens = 65536)

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                    .maxOutputTokens() shouldBe Optional.of(65536)
            }

            it("should omit max output tokens when not configured") {
                createGenerateConfig(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, VertexAIModelIdentifier.Gemini2_5Flash)
                    .maxOutputTokens() shouldBe Optional.empty()
            }

            it("should map thinkingLevel onto the thinking config") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should serialize thinkingLevel in uppercase proto-enum casing on the wire") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                val json =
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)
                        .toJson()

                json shouldContain "\"thinkingLevel\":\"LOW\""
                json shouldNotContain "\"thinkingLevel\":\"low\""
            }

            it("should map thinkingBudget onto the thinking config") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should throw when thinkingBudget is configured on a 3.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should throw when thinkingLevel is configured on a 2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)
                }.message shouldContain "not supported on Gemini 2.x models"
            }

            it("should map thinkingLevel MINIMAL to LOW on gemini-3.1-pro-preview") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_1ProPreview)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should map thinkingLevel MINIMAL to LOW on gemini-3.8-flash") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_8Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should map thinkingLevel MINIMAL to LOW on gemini-3.7-flash") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_7Flash)
                    .thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should allow thinkingBudget on a Custom gemini-2.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-2.5-x"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should throw when thinkingBudget is configured on a Custom gemini-3.x model") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        VertexAIModelIdentifier.Custom("gemini-3.9-flash"),
                    )
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should allow thinkingBudget on a Custom identifier with major version 1") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-1.5-pro"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should allow thinkingLevel on a Custom identifier that has no version match") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("my-proxy"),
                ).thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }

            it("should allow thinkingBudget on a Custom identifier with the models/ prefix") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("models/gemini-2.5-flash"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should throw when thinkingLevel is configured on a Custom identifier with the models/ prefix") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        VertexAIModelIdentifier.Custom("models/gemini-2.5-flash"),
                    )
                }.message shouldContain "not supported on Gemini 2.x models"
            }

            it("should allow thinkingLevel on a Custom identifier with major version 4") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.MINIMAL),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                ).thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "MINIMAL"
            }

            it("should throw when thinkingBudget is configured on a Custom identifier with major version 4") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                shouldThrow<Exception> {
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        generationSettings,
                        null,
                        VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                    )
                }.message shouldContain "only supported on Gemini 2.x models"
            }

            it("should allow thinkingBudget on a Custom gemini-exp-1206 identifier") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingBudget = 1024),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-exp-1206"),
                ).thinkingConfig()
                    .get()
                    .thinkingBudget() shouldBe Optional.of(1024)
            }

            it("should include default sampling parameters for a Gemini 2.x model when not configured") {
                val config = createGenerateConfig(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, VertexAIModelIdentifier.Gemini2_5Flash)

                config.temperature() shouldBe Optional.of(GenerationSettings.DEFAULT_TEMPERATURE)
                config.topP() shouldBe Optional.of(GenerationSettings.DEFAULT_TOP_P)
                config.topK() shouldBe Optional.of(GenerationSettings.DEFAULT_TOP_K.toFloat())
                config.candidateCount() shouldBe Optional.of(1)
            }

            it("should omit sampling parameters for a Gemini 3.x model when not configured") {
                val config = createGenerateConfig(emptyList(), emptyList(), GenerationSettings.DEFAULT, null, VertexAIModelIdentifier.Gemini3_5Flash)

                config.temperature() shouldBe Optional.empty()
                config.topP() shouldBe Optional.empty()
                config.topK() shouldBe Optional.empty()
                config.candidateCount() shouldBe Optional.empty()
            }

            it("should include an explicitly configured temperature for a Gemini 3.x model and still omit topP/topK") {
                val generationSettings = GenerationSettings.DEFAULT.copy(temperature = 0.5f)
                val config = createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)

                config.temperature() shouldBe Optional.of(0.5f)
                config.topP() shouldBe Optional.empty()
                config.topK() shouldBe Optional.empty()
            }

            it("should omit sampling parameters for a Custom Gemini 4.x model when not configured") {
                val config =
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        GenerationSettings.DEFAULT,
                        null,
                        VertexAIModelIdentifier.Custom("gemini-4.0-pro"),
                    )

                config.temperature() shouldBe Optional.empty()
                config.topP() shouldBe Optional.empty()
                config.topK() shouldBe Optional.empty()
                config.candidateCount() shouldBe Optional.empty()
            }

            it("should include default sampling parameters for a Custom identifier without a version match when not configured") {
                val config =
                    createGenerateConfig(
                        emptyList(),
                        emptyList(),
                        GenerationSettings.DEFAULT,
                        null,
                        VertexAIModelIdentifier.Custom("my-proxy"),
                    )

                config.temperature() shouldBe Optional.of(GenerationSettings.DEFAULT_TEMPERATURE)
                config.topP() shouldBe Optional.of(GenerationSettings.DEFAULT_TOP_P)
                config.topK() shouldBe Optional.of(GenerationSettings.DEFAULT_TOP_K.toFloat())
                config.candidateCount() shouldBe Optional.of(1)
            }

            it("should send an explicit temperature, topK and topP for a Gemini 2.5 model exactly as configured") {
                val generationSettings = GenerationSettings.DEFAULT.copy(temperature = 0.5f, topK = 5, topP = 0.9f)
                val config = createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini2_5Flash)

                config.temperature() shouldBe Optional.of(0.5f)
                config.topK() shouldBe Optional.of(5f)
                config.topP() shouldBe Optional.of(0.9f)
            }

            it("should send an explicit topP and topK for a Gemini 3.5 model") {
                val generationSettings = GenerationSettings.DEFAULT.copy(topK = 5, topP = 0.9f)
                val config = createGenerateConfig(emptyList(), emptyList(), generationSettings, null, VertexAIModelIdentifier.Gemini3_5Flash)

                config.topK() shouldBe Optional.of(5f)
                config.topP() shouldBe Optional.of(0.9f)
            }

            it("should allow thinkingLevel on a Custom gemini-exp-1206 identifier") {
                val generationSettings =
                    GenerationSettings.DEFAULT.copy(
                        thinkingConfig = ThinkingConfig(thinkingLevel = ThinkingLevel.LOW),
                    )

                createGenerateConfig(
                    emptyList(),
                    emptyList(),
                    generationSettings,
                    null,
                    VertexAIModelIdentifier.Custom("gemini-exp-1206"),
                ).thinkingConfig()
                    .get()
                    .thinkingLevel()
                    .get()
                    .toString() shouldBe "LOW"
            }
        }

        describe("optional parameters") {

            val optionalParameters =
                listOf(
                    Parameter.Primitive("name", null, isRequired = true, type = ParameterType.Primitive.String),
                    Parameter.Primitive("age", null, isRequired = false, type = ParameterType.Primitive.Integer),
                    Parameter.Complex.Enum(
                        name = "unit",
                        description = null,
                        isRequired = false,
                        default = PrimitiveValue.String("MM"),
                        values = listOf(PrimitiveValue.String("MM"), PrimitiveValue.String("CM")),
                        valueType = ParameterType.Primitive.String,
                    ),
                    Parameter.Complex.Object(
                        name = "size",
                        description = null,
                        isRequired = false,
                        parameters = listOf(Parameter.Primitive("value", null, isRequired = true, type = ParameterType.Primitive.Number)),
                    ),
                    Parameter.Complex.Array(
                        name = "tags",
                        description = null,
                        isRequired = false,
                        itemDefinition = Parameter.Primitive("Item", null, isRequired = true, type = ParameterType.Primitive.String),
                    ),
                )

            it("should add null to the type of optional fields of the response schema and leave them out of required") {
                val responseParameter = Parameter.Complex.Object("Extraction", null, isRequired = true, parameters = optionalParameters)

                createGenerateConfig(emptyList(), emptyList(), GenerationSettings.DEFAULT, responseParameter, VertexAIModelIdentifier.Gemini2_5Flash)
                    .responseJsonSchema()
                    .get()
                    .toString()
                    .let { Json.parseToJsonElement(it) } shouldBe
                    Json.parseToJsonElement(
                        """
                        {
                          "type": "object",
                          "properties": {
                            "name": { "type": "string" },
                            "age": { "type": ["integer", "null"] },
                            "unit": { "type": ["string", "null"], "enum": ["MM", "CM", null] },
                            "size": {
                              "type": ["object", "null"],
                              "properties": { "value": { "type": "number" } },
                              "required": ["value"]
                            },
                            "tags": { "type": ["array", "null"], "items": { "type": "string" } }
                          },
                          "required": ["name"]
                        }
                        """,
                    )
            }

            it("should mark optional tool parameters nullable and leave them out of required") {
                val tool =
                    object : ToolDescription {
                        override val name = ToolName("extract")
                        override val description: String? = null
                        override val parameters = optionalParameters
                    }

                val schema =
                    createGenerateConfig(emptyList(), listOf(tool), GenerationSettings.DEFAULT, null, VertexAIModelIdentifier.Gemini2_5Flash)
                        .tools()
                        .get()
                        .single()
                        .functionDeclarations()
                        .get()
                        .single()
                        .parameters()
                        .get()
                val properties = schema.properties().get()

                schema.required() shouldBe Optional.of(listOf("name"))
                properties.getValue("name").nullable() shouldBe Optional.empty()
                listOf("age", "unit", "size", "tags").forEach {
                    properties.getValue(it).nullable() shouldBe Optional.of(true)
                }
                properties.getValue("size").required() shouldBe Optional.of(listOf("value"))
            }
        }
    })
