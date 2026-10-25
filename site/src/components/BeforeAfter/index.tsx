import clsx from "clsx";
import CodeLines from "../CodeLines";
import PixelMark from "../PixelMark";
import styles from "./styles.module.css";

const BY_HAND = `// Simplified: one provider, one tool, happy path only
val tools = buildJsonArray {
    addJsonObject {
        put("type", "function")
        putJsonObject("function") {
            put("name", "getWeather")
            putJsonObject("parameters") {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("city") { put("type", "string") }
                }
            }
        }
    }
}

val messages = mutableListOf<ChatMessage>(UserMessage("Weather in Amsterdam?"))
while (true) {
    val reply = http.post("https://api.openai.com/v1/chat/completions") {
        bearerAuth(apiKey)
        setBody(ChatRequest("gpt-5.5", messages, tools))
    }.body<ChatResponse>().choices.first().message
    messages += reply
    val call = reply.toolCalls?.firstOrNull() ?: break
    val args = Json.decodeFromString<WeatherRequest>(call.function.arguments)
    messages += ToolMessage(call.id, currentWeather(args.city))
}

// Hope the last reply is valid JSON in the right shape
val weather = Json.decodeFromString<Weather>(messages.last().content!!)`;

const WITH_AIGENTIC = `@AigenticParameter
data class WeatherRequest(val city: String)

val weatherAgent = agent<String, Weather> {
    openAIModel {
        apiKey(apiKey)
        modelIdentifier(OpenAIModelIdentifier.GPT5_5)
    }
    task("Answer questions about the weather") {}
    addTool("getWeather") { req: WeatherRequest ->
        currentWeather(req.city)
    }
}

val run = weatherAgent.start("Weather in Amsterdam?")`;

const COLUMNS = [
  {
    title: "By hand",
    note: "Simplified sketch",
    code: BY_HAND,
    points: [
      "JSON schemas written by hand",
      "Your own tool-call loop",
      "Parse the reply and hope it's valid",
      "Rewrite it all for another provider",
    ],
    highlighted: false,
  },
  {
    title: "With Aigentic",
    note: "Same agent",
    code: WITH_AIGENTIC,
    points: [
      "Schemas come from your data classes",
      "Aigentic runs the loop for you",
      "A typed Weather back, or Stuck or Fatal",
      "Change one block to switch provider",
    ],
    highlighted: true,
  },
];

export default function BeforeAfter() {
  return (
    <div className={styles.comparison}>
      {COLUMNS.map((column) => (
        <div key={column.title} className={clsx(styles.column, column.highlighted && styles.highlighted)}>
          <div className={styles.header}>
            <h3>
              {column.highlighted && <PixelMark />}
              {column.title}
            </h3>
            <span>{column.note}</span>
          </div>
          <div className={styles.frame}>
            <CodeLines code={column.code} />
          </div>
          <ul className={styles.points}>
            {column.points.map((point) => (
              <li key={point}>
                {column.highlighted ? <PixelMark /> : <span className={styles.dash} aria-hidden="true" />}
                {point}
              </li>
            ))}
          </ul>
        </div>
      ))}
    </div>
  );
}
