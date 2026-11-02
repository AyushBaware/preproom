# PrepRoom

> AI-powered interview preparation for a **specific company and role**. Practice alone, or with other candidates in a live quiz room.

*Working title. Status: environment setup done, building starts at Week 1.*

---

## 1. What is PrepRoom?

Today, a candidate copies a job description (JD) and a resume into a chatbot and asks it to quiz them. PrepRoom turns that manual routine into one product:

1. Pick a **company and role**.
2. Give PrepRoom the **job description (JD)**: paste it, upload a PDF, paste a job-page link, or let it look the JD up when the company publishes it openly. It always shows where the JD came from.
3. Upload your **resume**. You get a **gap report**: where you match and where you lack, with evidence.
4. An **AI interviewer** asks role-specific questions, adapts to your answers, and grades them.
5. Ask follow-ups such as *"Where am I weakest?"*
6. Later: candidates preparing for the same company join a **live group quiz**.

**Why not just use a chatbot?**
- It fetches the JD for you and shows the source.
- Company packs (JD analysis and question bank) are cached and shared, which saves time and AI cost.
- Progress is tracked across sessions.
- We measure how well the AI grader agrees with a human, instead of trusting it blindly.
- JD and PDF text is treated as untrusted, and the AI's abilities are limited.

---

## 2. How the pieces connect

```
Browser (React)
     |
  Nginx  ........ load balancer: spreads traffic over several copies
     |
  Gateway (x2) .. login check + rate limiting (counters kept in Redis)
     |
  +--> Express API ........ login, user profiles
  |
  +--> Spring Boot engine (x2) ... the "brain"
          |-- Redis ........ cache, queue, daily AI budgets, live-room messaging
          |-- MongoDB Atlas . users, company packs, sessions, results
          |-- LLMs .......... Groq (main), Gemini (fallback), via Spring AI
          |-- MCP tools ..... standard tools the AI agent can call
```

*Final wiring (for example, exactly how login tokens flow) is decided as we build.*

**What happens when a user clicks "Start interview"**

1. React sends the request.
2. Nginx picks one gateway copy.
3. The gateway checks the login token and the rate limit (a counter in Redis).
4. The engine checks the user's daily AI budget (Redis) and loads the company pack (Redis cache, else MongoDB).
5. The engine asks the LLM through Spring AI. The agent may call tools.
6. Slow jobs, like a full report, go on a queue (Redis) and a worker picks them up.
7. Results are saved in MongoDB and shown to the user.

---

## 3. Why each piece exists

| Piece | What it is | Why PrepRoom needs it |
|---|---|---|
| **React** | Web interface | Where users interact |
| **Express (Node)** | Small API | Login and profiles |
| **Spring Boot engine** | Java backend | AI orchestration, grading, scoring, live game logic |
| **Spring AI** | Java library | One interface to LLMs, tool calling, embeddings, MCP |
| **Nginx (load balancer)** | Traffic splitter | Spreads load and keeps working if one copy dies |
| **API gateway** | Single front door | Checks login and enforces rate limits |
| **Rate limiter** | Request counter (token bucket) | Stops one user or bot from flooding the system |
| **Redis** | Fast in-memory store | Shared counters, cache, queue, dedup keys, live-room messages |
| **Queue** | Waiting line for jobs | Slow work doesn't block users, bursts are absorbed |
| **MongoDB Atlas** | Database | Permanent data |
| **RAG** | Search, then answer | AI answers from your resume and JD, not from guesses |
| **Agent** | AI that uses tools | Picks the next question, fetches the JD |
| **MCP** | Tool standard | One consistent way to expose tools to the agent |
| **ML model** | Small trained model | Learns question difficulty and skill level, compared to a simple baseline |
| **Circuit breaker** | Safety switch | If the LLM is slow or down, fall back instead of crashing |
| **Guardrails** | Limits and checks | Untrusted text, step limits, no code execution |

---

## 4. Problems we are designing for

| Situation | What handles it |
|---|---|
| Many users start at once | Nginx spreads them, the gateway limits abusers, the queue absorbs bursts |
| Free AI quotas are small | Per-user daily AI budget (Redis) and cached company packs |
| The AI provider is slow or down | Circuit breaker plus a fallback model |
| A JD contains hidden instructions | Treated as data, never as commands |
| One server dies | Load balancer sends traffic to the other copy (tested in our Docker lab) |

---

## 5. Tech stack and tools

| Area | Choice |
|---|---|
| Languages | Java 21, JavaScript, Python (ML notebook only), YAML and Nginx config |
| Backend | Spring Boot 4.0.x, Spring AI 2.0.x, Spring Cloud 2025.1.x (Gateway, circuit breaker), Express on Node 22 |
| Frontend | React |
| Data | MongoDB Atlas (free), Redis |
| AI | Groq (main), Gemini free tier (fallback and embeddings), RAG, MCP, scikit-learn |
| Infrastructure | Docker Desktop (WSL 2), Docker Compose, Nginx |
| Dev tools | VS Code (Java and Spring Boot extension packs), Git and GitHub, Maven, JUnit |
| Testing | JUnit, k6 for load tests, a labeled evaluation set |
| Hosting | Vercel (frontend), Render (backend demo) |

*Exact versions are pinned and tested together in Week 1. Never mix versions untested.*

**Free-tier limits we design around**

| Service | Limit that matters |
|---|---|
| Groq (free) | About 200K tokens per day per model, 8K tokens per minute |
| Gemini (free) | Flash about 20 requests/day, Flash-Lite about 500/day. Prompts may be used by Google, so synthetic data only |
| Atlas (free) | 512 MB storage, 100 operations/second |
| Render (free) | 512 MB RAM, sleeps after 15 minutes idle |
| Laptop | 12 GB RAM, 2 cores. Docker VM capped at 2 GB |

---

## 6. Build plan

| Week | Goal |
|---|---|
| 0 | Validation tests: JD fetch for 5 companies, Spring Boot on Render, real calls to Groq and Gemini, Atlas index test |
| 1 | Project shell, login, resume upload, pasted JD, LLM provider switch |
| 2 | JD intake ladder: paste, PDF upload, URL fetch, public job-board lookup, graceful fallback; company pack cache; prompt-injection guard |
| 3 | RAG, gap report, "where am I lacking?" chat |
| 4 | Adaptive interviewer, grader, MCP tools, session storage |
| 5 | Gateway rate limiter, queue and workers, circuit breaker, Nginx with 2 copies |
| 6 | ML model, grader agreement test, load test. **Gate: solo mode must be solid** |
| 7-9 | Live group room: WebSockets, Redis pub/sub, adaptive host, join-storm test |
| 10 | Privacy pass, tests, deploy |
| 11 | README with numbers, demo video, resume bullets |

If time runs short, cut in this order: the live room, then live host adaptivity, then the learned ML model (keep the baseline).

---

## 7. Status

**Done**
- WSL 2 and Docker Desktop installed, memory capped at 2 GB
- VS Code with Java and Spring Boot extension packs
- Free accounts: MongoDB Atlas (PrepRoom project, Mumbai), Google AI Studio, Groq
- Docker lab: Redis with a volume, Docker Compose, Nginx load balancing with a tested failover
- Spring Boot 4.1 engine with Spring AI 2.0 calling Groq, and a MongoDB Atlas connection, both tested locally
- First version deployed to Render's free tier: about 37 s startup, about 54 s wake-up after idle

**Next**
- Week 1: resume PDF text extraction, structured JD extraction from text, storage in MongoDB, LLM provider fallback

---

## 8. Repository layout

```
preproom/
  web/       React frontend
  api/       Express API (login, profiles)
  engine/    Spring Boot core
  gateway/   Spring Cloud Gateway
  infra/     Docker Compose and Nginx files
  eval/      test sets and results
  docs/      notes and diagrams
```

---

## 9. Daily commands

| Task | Command |
|---|---|
| Start infrastructure | Open Docker Desktop, then `docker compose up -d` |
| See what runs | `docker compose ps` |
| Read logs | `docker compose logs <service>` |
| Stop (keep data) | `docker compose down` |
| Stop (delete data) | `docker compose down -v` |
| End of session | Quit Docker Desktop, then `wsl --shutdown` |

---

## 10. How we will prove it works

- JD fetch success rate across target companies
- Gap-report accuracy against about 30 hand-labeled resume and JD pairs
- AI grader agreement with human grades on 50 to 100 answers
- ML model compared with a simple baseline
- Rate limiter returns `429` beyond the limit
- Failover test: kill one app copy, requests keep succeeding
- Queue behavior under a burst, and load-test numbers with the laptop's specs stated

---

## 11. Honest limits

- Free AI quotas are small, so evaluation runs are spread over days.
- A 2-core laptop gives modest load-test numbers. We report them with the hardware stated.
- We never claim "Company X always asks Y". Questions come from the JD and the role.
- Resumes are personal data. Free-tier AI gets synthetic data only, and the app stores the minimum.
- The public demo runs a single instance. The multi-copy setup is shown in a recorded demo.

## Measured so far (laptop: 2 cores, 12 GB RAM)

| What | Result |
|---|---|
| JD extraction, first time | 5 to 15 s (Groq) |
| JD extraction, cached | 0.4 s |
| Gap report | about 8 s |
| Render free tier startup | 74 s; first request after idle waits about 90 s |
| Gap report vs my own judgement | 9 of 9 on one pair (small sample) |