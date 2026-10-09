# RescueDesk AI

## Product Requirements Document (PRD)

**Version:** 1.0
**Product Type:** Android Mobile Application
**Product Category:** Public-benefit, Offline-First Emergency Preparedness and Guidance
**Initial Market:** Philippines
**Primary Language:** Filipino and English
**Target Devices:** Android phones with 4–6 GB RAM or more
**Business Model:** Free household app supported by grants, CSR sponsorships, NGOs, LGUs, schools, and institutional deployments

---

# 1. Product Overview

## 1.1 Product Vision

RescueDesk AI is an offline-first emergency preparedness application designed to help Filipino households prepare for and respond to common emergencies, including typhoons, floods, earthquakes, fires, and other disasters.

The application combines:

* An on-device AI assistant.
* A verified emergency information library.
* Family emergency plans.
* Emergency checklists.
* Saved emergency contacts.
* Offline access to essential safety guidance.
* Simple, accessible, senior-friendly mobile interfaces.

Users can download the AI model and emergency knowledge packs while connected to the internet. Once the necessary files are downloaded, the app's core features should continue working without internet access.

**The application must not require users to understand AI, technical settings, or complicated menus to get help.**

## 1.2 Core Value Proposition

"Reliable emergency guidance, ready when you need it—even without internet."

RescueDesk AI helps people prepare before disasters happen, remember what to do during emergencies, and find practical instructions without searching through multiple websites or documents.

## 1.3 Product Principles

1. **Simple before smart:** Common emergency actions must be easier to access than the AI chat.
2. **Offline first:** Essential safety information must remain available without connectivity.
3. **Human-friendly:** Use short sentences, familiar words, icons with text labels, and clear instructions.
4. **Safety before engagement:** Never invent emergency alerts, evacuation orders, official instructions, or medical advice.
5. **Accessible to all ages:** Design for children, adults, and senior citizens.
6. **Privacy by default:** Personal household information stays on the device unless the user explicitly chooses to share it.
7. **Free essential protection:** Do not place core emergency guidance behind a subscription or payment.
8. **Honest information:** Show where guidance came from and when the information was last reviewed.

---

# 2. Goals and Success Criteria

## 2.1 Product Goals

* Provide essential emergency information without an internet connection.
* Make the app understandable to first-time smartphone users.
* Help households create practical emergency preparedness plans.
* Provide AI answers based on approved, locally stored information.
* Make the application useful even if the AI model is not installed or temporarily unavailable.
* Support English and Filipino from the initial release, subject to content review.
* Establish a platform that can be adopted by schools, communities, NGOs, and local government units.

## 2.2 MVP Success Metrics

These are proposed pilot targets, not existing results.

| Metric                                                     |                             Target |
| ---------------------------------------------------------- | ---------------------------------: |
| First-time users who finish basic setup                    |                       At least 90% |
| Users who can find an emergency guide in usability testing |                       At least 90% |
| Essential offline features working in airplane mode        |        100% of approved test cases |
| AI answers meeting the approved grounding criteria         | At least 95% of a defined test set |
| Users who complete a family emergency plan                 | At least 70% of pilot participants |
| Unresolved critical safety defects before public release   |                                  0 |
| Users who can use the primary interface without assistance |  At least 85% in usability testing |

AI quality targets must be measured using a reviewed test set. A high answer score must never be treated as proof that every AI response is safe.

---

# 3. Target Users and Personas

## 3.1 Senior Citizen — "Aling Rosa"

**Needs**

* Large text and buttons.
* Few steps to reach emergency information.
* Clear instructions without technical terms.
* Strong contrast and readable screens.
* Minimal typing.

**Design requirements**

* Adjustable text size.
* Touch targets preferably 56 dp or larger for primary actions.
* No icon-only navigation for important actions.
* Read-aloud support where feasible.
* Short, numbered instructions.

## 3.2 Parent or Household Organizer — "Mark"

**Needs**

* Prepare the family before a disaster.
* Keep emergency contacts and meeting points.
* Track go-bag supplies.
* Know what to do when family members are separated.

**Design requirements**

* A guided family setup.
* Save progress automatically.
* Checklists with completion indicators.
* Printable or shareable family plan where feasible.

## 3.3 Young Adult — "Jessa"

**Needs**

* Fast answers.
* Modern, easy-to-understand UI.
* A quick way to ask questions.
* Clear access to emergency checklists.

**Design requirements**

* Search and AI chat.
* Quick-access emergency categories.
* Concise answers with expandable explanations.
* Fast navigation without unnecessary onboarding.

## 3.4 Child or Teenager — "Miguel"

**Needs**

* Understand basic emergency actions.
* Know how to contact a trusted adult.
* Learn preparedness without being frightened.

**Design requirements**

* Simple wording.
* Friendly illustrations and visual checklists.
* Age-appropriate educational content.
* No unnecessary graphic disaster imagery.
* Clearly indicate when to ask a trusted adult for help.

The application is not intended to replace adult supervision or emergency responders.

## 3.5 Community Volunteer — "Barangay Volunteer"

**Needs**

* Share approved preparedness materials.
* Help residents install and configure the app.
* Explain basic emergency procedures.

**Design requirements**

* Easy onboarding.
* Downloadable content packs.
* A clear content version and review date.
* Optional organization-provided setup materials in future releases.

---

# 4. UI/UX Design System

## 4.1 Overall Visual Direction

The interface should look like a modern public-service application: calm, welcoming, trustworthy, and easy to understand.

Avoid:

* A cluttered dashboard.
* Too many colorful cards.
* Tiny text and buttons.
* Complicated technical terminology.
* Excessive animations.
* Dark, intimidating disaster imagery.
* Making AI chat the only way to find information.

Use:

* A light background.
* Dark, high-contrast text.
* A consistent emergency accent color.
* Simple line icons paired with labels.
* Clear cards and section spacing.
* Illustrations that explain actions rather than decorate empty space.

## 4.2 Suggested Color Palette

| Purpose              | Color      | Hex       |
| -------------------- | ---------- | --------- |
| Primary brand        | Deep blue  | `#174A7E` |
| Primary action       | Blue       | `#246BCE` |
| Safe/prepared status | Green      | `#247A50` |
| Caution              | Amber      | `#9A5B00` |
| Urgent warning       | Dark red   | `#B42318` |
| Main background      | Soft white | `#F7F9FC` |
| Card background      | White      | `#FFFFFF` |
| Main text            | Near-black | `#17212B` |
| Secondary text       | Dark gray  | `#526170` |
| Borders              | Light gray | `#D8E0E8` |

**Color must never be the only way to communicate status.** Include labels and icons, such as "Offline," "Needs attention," or "Verified guide."

Red should be reserved for genuine urgent warnings, not ordinary buttons or decorative highlights.

## 4.3 Typography

Use a highly legible Android system font, such as Roboto.

| Element                         |              Recommended size |
| ------------------------------- | ----------------------------: |
| Main screen heading             |                      28–32 sp |
| Section heading                 |                      22–24 sp |
| Primary card title              |                      18–20 sp |
| Body text                       |                 18 sp default |
| Supporting text                 | 16 sp minimum where practical |
| Button label                    |                         18 sp |
| Important emergency instruction |                      20–24 sp |

Requirements:

* Support Android system font scaling.
* Prevent text from clipping at larger accessibility sizes.
* Use short paragraphs.
* Prefer numbered steps over long blocks of text.
* Avoid using all-capital sentences except for short labels.

## 4.4 Touch and Interaction Requirements

* Standard touch targets must be at least 48 × 48 dp.
* Important primary actions should preferably be 56 dp or larger.
* Leave adequate spacing between adjacent buttons.
* Use clear pressed, selected, disabled, and loading states.
* Never require swipe gestures for essential emergency actions.
* Provide text labels for important icons.
* Avoid double-tap requirements and hidden gestures.
* Confirm destructive actions such as deleting a saved family plan.

## 4.5 Accessibility Requirements

* Compatible with Android TalkBack.
* Respect system font scaling.
* Support high-contrast readable text.
* Provide meaningful accessibility labels for buttons and images.
* Do not convey critical information using color alone.
* Avoid flashing content and unnecessary motion.
* Use simple Filipino and English wording.
* Make error messages explain what happened and what the user can do next.

## 4.6 Navigation Structure

Use a bottom navigation bar with four destinations:

1. **Home**
2. **Guides**
3. **Ask AI**
4. **My Family**

Place **Emergency Help** as a prominent, consistently accessible action on the Home screen and relevant emergency screens. It should not be a hidden fifth navigation destination.

Settings, downloads, language, accessibility, privacy, and app information should be reachable from a clearly labeled menu on Home.

---

# 5. Detailed Screen Requirements

## 5.1 Screen A — First Launch and Welcome

### Purpose

Explain the application in a few seconds and help the user begin without requiring an account.

### Layout

1. RescueDesk AI logo.
2. Friendly illustration showing a prepared household.
3. Heading: **"Handa ka ba sa emergency?"**
4. Supporting text: "Mga gabay na maaari mong buksan kahit walang internet."
5. Primary button: **"Magsimula"**
6. Secondary link: "Learn about RescueDesk AI"

### Behavior

* No mandatory registration.
* No email or phone number required.
* No lengthy introductory slides.
* Show the language choice during the initial setup.
* Explain that emergency guidance is not a replacement for official authorities or professional responders.

### Acceptance Criteria

* A first-time user can start setup with one tap.
* The welcome screen works without network access.
* Users can return to the welcome screen only when appropriate; ordinary navigation must not restart onboarding.

## 5.2 Screen B — Language Selection

### Purpose

Allow users to select a comfortable language.

### Layout

Heading: **"Choose your language"**

Options:

* Filipino
* English

Each option should be presented as a large, labeled selection card.

Primary button: **"Continue"** / **"Magpatuloy"**

### Requirements

* Language can be changed later in Settings.
* All navigation labels must be translated consistently.
* Emergency content must have a clear language label.
* If a guide is not available in the selected language, tell the user rather than silently presenting a potentially confusing translation.
* AI responses should follow the selected language when possible.

## 5.3 Screen C — Accessibility Setup

### Purpose

Let users choose a comfortable reading experience without making accessibility setup feel like a medical questionnaire.

### Layout

Heading: **"Make the app comfortable for you"**

Options:

* Standard text
* Larger text
* Extra-large text

Optional controls:

* Read instructions aloud
* High-contrast appearance

Primary button: **"Continue"**

Secondary option: **"Use default settings"**

### Requirements

* All options are optional.
* The user can change them later.
* Text must reflow rather than clip when enlarged.
* Read-aloud functionality should use Android text-to-speech where available.
* If speech services are unavailable, written instructions remain usable.

## 5.4 Screen D — Download and Offline Setup

### Purpose

Help users download the AI model and emergency knowledge pack without confusing them about what is required.

### Layout

Heading: **"Prepare your offline assistant"**

Show two separate items:

**1. Emergency Guide Pack**

* Includes essential emergency instructions.
* Smaller download than the AI model where practical.
* Must be prioritized because it provides essential information even without AI.

**2. On-Device AI Model**

* Enables natural-language questions offline.
* Requires additional storage and memory.
* Optional for accessing the basic emergency library.

For each item, display:

* Approximate download size.
* Available storage check.
* Download button.
* Progress percentage.
* Pause/resume or retry controls when supported.
* Download status.
* Version information.

Primary action: **"Download emergency guides"**

Secondary action: **"Download AI model"**

Provide a visible option to skip AI setup.

### Behavior

* Download over Wi-Fi by default, with a clear option to use mobile data.
* Warn users before large downloads.
* Resume interrupted downloads where technically supported.
* Verify file integrity before activating a downloaded model or content pack.
* Keep a previously valid version available until a replacement has been verified, subject to available storage.
* Never describe the app as fully AI-enabled offline before the model is successfully installed.
* Never block essential guide access while a model is downloading.

### Storage Warning

If storage is insufficient, explain:

* How much space is needed.
* How much space is available.
* Which optional downloads can be skipped.

### Acceptance Criteria

A user can skip the model download, use the emergency guides, and later download the AI model from Settings or the model status screen.

## 5.5 Screen E — Home Dashboard

### Purpose

Provide a clear starting point for ordinary preparedness and urgent situations.

### Layout Specification

**Top section**

* RescueDesk AI logo and name.
* Current offline/online status.
* Settings icon with an accessible label.

**Welcome message**

* "Kumusta! Handa ka na ba?"
* Supporting line: "Your emergency guides are ready when you need them."

**Primary emergency action**

* Large button: **"Emergency Help"**
* Supporting text: "Find immediate safety guidance"

**Quick-access guide cards**

Display four large cards:

1. **Bagyo at Baha** — Typhoon and Flood
2. **Lindol** — Earthquake
3. **Sunog** — Fire
4. **First Aid** — Basic First Aid

Each card includes an icon, title, and short description.

**Preparedness card**

* "My Family Plan"
* Progress such as "3 of 5 sections completed"
* Button: "Continue plan"

**AI card**

* "May tanong ka?"
* Button: "Ask RescueDesk AI"

**Offline status**

* Example: "Emergency guides available offline"
* AI status: "AI model installed" or "Download AI model to enable offline questions"

### Visual Hierarchy

The Emergency Help action must be more visually prominent than optional features. The home screen should not look like an analytics dashboard.

### Acceptance Criteria

A first-time user can locate an emergency guide without opening the AI chat, navigating settings, or creating an account.

## 5.6 Screen F — Emergency Help

### Purpose

Give users a direct path to relevant safety instructions when they may be stressed or in danger.

### Layout

Heading: **"What is happening?"**

Present large, clearly labeled choices:

* Flooding or rising water
* Typhoon or strong winds
* Earthquake
* Fire or smoke
* Injury or medical emergency
* Other emergency

Include a prominent note:

**"If you are in immediate danger, move to safety if possible and contact local emergency services."**

Only display a specific emergency number when its source, geographic applicability, and last review date are recorded.

### Flow

1. User selects an emergency category.
2. App opens a concise guide for that situation.
3. The guide starts with the most important immediate safety actions.
4. Additional details appear below.
5. User can open related guidance or saved emergency contacts.

### Requirements

* Do not force users to type a question.
* Do not require AI to open an emergency guide.
* Avoid unnecessary confirmation dialogs before showing instructions.
* Do not automatically call emergency services without explicit user action.
* Do not claim that the app dispatches rescue personnel.
* Where instructions depend on location or conditions, clearly state those limitations.

## 5.7 Screen G — Emergency Guide Details

### Purpose

Present practical, easy-to-follow emergency instructions.

### Layout

1. Guide title.
2. Category icon and short summary.
3. Source and last-reviewed date.
4. Section titled **"Do this first"**.
5. Numbered instructions.
6. Section titled **"Avoid these actions"**, where applicable.
7. Additional preparation steps.
8. Related guides.
9. Optional "Read aloud" button.

### Content Rules

* Put the most important actions first.
* Use one clear action per numbered step where possible.
* Distinguish general preparedness advice from immediate emergency instructions.
* Explain unfamiliar terms.
* Use reviewed information from appropriate authorities or qualified organizations.
* Show a content warning if the guide is outdated or requires review.
* Avoid unsupported guarantees such as "This will keep you completely safe."

### Example Structure

**Flooding: What to do**

**Do this first**

1. Move away from rising water when it is safe to do so.
2. Follow official evacuation instructions applicable to your area.
3. Avoid walking or driving through floodwater.

**Important**

* Conditions vary by location.
* Follow current official instructions where available.

This is an illustrative interface example, not a complete operational flood-response guide. Final safety content must be reviewed before release.

## 5.8 Screen H — Ask RescueDesk AI

### Purpose

Allow users to ask questions in everyday language and receive answers based on downloaded, approved information.

### Layout

**Header**

* Title: "Ask RescueDesk AI"
* Status: "Working offline" or "Internet connected"
* Model readiness indicator.

**Suggested question chips**

* "What should I put in a go-bag?"
* "What should my family prepare before a typhoon?"
* "How can I prepare our house for an earthquake?"
* "Help me make a family emergency plan."

**Chat area**

* Large readable text.
* Short paragraphs and numbered steps.
* Source cards beneath answers.
* Clear indication when the answer is based on saved guidance.

**Input area**

* Text field: "Type your question..."
* Large Send button.
* Optional microphone button in a future version.

### AI Response Structure

Where applicable, each answer should contain:

1. **Short answer:** The most important point.
2. **What to do:** Practical steps.
3. **Important caution:** Relevant limitations or warnings.
4. **Sources:** Titles and review dates of the saved guidance used.

### Example Response Style

User: "Ano ang dapat ihanda bago ang bagyo?"

Assistant:

* Prepare drinking water, food, essential medicines, a flashlight, batteries, and important documents.
* Charge phones and power banks while electricity is available.
* Know your household meeting point and follow local official advisories.

Sources: Display the actual approved guides used by the retrieval system.

The example illustrates the desired tone. The final answer must be generated from the actual locally stored knowledge sources.

### AI Safety Behavior

The AI must:

* Use retrieved approved information whenever the answer concerns emergency safety.
* Clearly state when relevant information cannot be found.
* Never invent current weather, live warnings, evacuation orders, road closures, or rescue availability.
* Never claim it has contacted authorities or sent an SOS unless a separately implemented service confirms the action.
* Distinguish saved general guidance from current official information.
* Escalate dangerous or uncertain questions to appropriate official or professional help.
* Avoid diagnosing medical conditions.
* Never present an unverified generated answer as official guidance.

### Fallback State

If the AI model is missing, incompatible, or unable to load, display:

**"Offline AI is not ready on this device."**

Actions:

* Open emergency guides.
* Retry AI setup.
* Check model requirements.

Never show an endless loading animation or a blank chat screen.

## 5.9 Screen I — Guides Library

### Purpose

Provide direct access to emergency information without requiring users to interact with AI.

### Layout

* Search field: "Search emergency guides"
* Category cards.
* Recently opened guides.
* Saved guides.
* Downloaded content status.

### Initial Categories

* Typhoon and Flood
* Earthquake
* Fire Safety
* Basic First Aid
* Household Preparedness
* Emergency Communication
* Evacuation Preparation
* After a Disaster

### Search Behavior

* Search locally using a lightweight text index.
* Support common terms in Filipino and English.
* Handle simple spelling mistakes where practical.
* Display relevant guide titles before requiring the user to type a complete question.
* Work offline.

### Acceptance Criteria

The guide library remains usable if the AI model is deleted, unavailable, or unsupported by the device.

## 5.10 Screen J — My Family

### Purpose

Help users prepare a household emergency plan without requiring them to create an online account.

### Main Sections

1. Family Emergency Plan
2. Emergency Contacts
3. Meeting Places
4. Go-Bag Checklist
5. Important Reminders

### Family Plan Wizard

Use a step-by-step wizard rather than a long form.

**Step 1: Household**

* Household nickname (optional).
* Number of household members.
* Optional notes about general preparedness needs.

**Step 2: Emergency Contacts**

* Contact name.
* Relationship.
* Phone number.
* Optional secondary contact.

**Step 3: Meeting Places**

* Nearby meeting place.
* Alternative meeting place.
* Out-of-area contact, if available.

**Step 4: Supplies**

* Drinking water.
* Food.
* Medicines.
* Flashlight.
* Batteries or power bank.
* Hygiene supplies.
* Important documents.
* Other household-specific supplies.

**Step 5: Review**

* Show the saved plan.
* Allow editing.
* Mark the plan complete.

### Privacy Requirements

* Store personal information locally by default.
* Do not upload household contacts or family details automatically.
* Do not request exact GPS location as a requirement for using the app.
* Explain any Android permissions before requesting them.
* Allow users to remove saved data.

### Emergency Contacts Behavior

* Provide a visible call action for a selected contact.
* Show the phone number before or during the calling flow as appropriate.
* Require a deliberate user action before initiating a call.
* Clearly indicate that calling requires a working phone service.
* Never imply that saved contacts are automatically notified.

## 5.11 Screen K — Go-Bag Checklist

### Purpose

Make preparedness actionable and easy to track.

### Layout

* Heading: "Prepare your emergency bag"
* Progress indicator.
* Checklist grouped by category.
* Large checkboxes.
* "Add an item" action.
* Last-updated date.

### Categories

* Water and Food
* First Aid and Medicines
* Lighting and Communication
* Documents and Money
* Clothing and Hygiene
* Special Household Needs

### Behavior

* Save progress immediately.
* Work offline.
* Allow users to add, edit, and remove custom items.
* Provide an optional reminder feature in a future version.
* Avoid implying that one checklist is suitable for every household or emergency.

## 5.12 Screen L — Offline and Download Manager

### Purpose

Make the offline capabilities understandable and maintainable.

### Layout

**Emergency Guide Pack**

* Installed version.
* Last reviewed date or pack review information.
* Storage used.
* Update availability, when known.

**AI Model**

* Model name.
* Installed version.
* Approximate storage used.
* Device compatibility status.
* Update button.
* Remove model button.

**Connection status**

* Online.
* Offline.
* Download paused.
* Update available.

### Requirements

* Keep essential guide content separate from the model.
* Explain that the AI model requires more device memory than the guide library.
* Verify downloads before use.
* Do not delete a valid model until a replacement has passed integrity checks.
* Do not delete user plans when updating the model.
* Warn users before removing an installed model.
* Preserve the guide library if model removal occurs.

## 5.13 Screen M — Settings and Privacy

### Sections

**Appearance and Accessibility**

* Text size.
* High-contrast appearance.
* Read-aloud preference.

**Language**

* Filipino.
* English.

**Offline Resources**

* Model management.
* Guide pack management.
* Storage information.

**Privacy**

* Explain what is stored locally.
* Clear chat history.
* Delete household plan.
* Delete all locally saved personal information.

**About**

* App version.
* Model version.
* Content pack version.
* Safety disclaimer.
* Source and content review information.
* Feedback and report-a-problem options.

### Requirements

* Use plain language.
* Explain the consequences of deleting information.
* Do not bundle unrelated permissions into a single request.
* No account should be required for core functionality.

---

# 6. Core Functional Requirements

## FR-01: Offline Emergency Library

The application must provide access to approved emergency guidance without internet access.

**Acceptance criteria**

* Guides open in airplane mode.
* Search works locally.
* Guide sources and available review dates are displayed.
* The guide library remains usable without the AI model.

## FR-02: On-Device AI

The application must run the selected compatible language model locally on supported Android devices.

**Acceptance criteria**

* No cloud inference is required for the offline chat.
* The UI indicates when the model is not ready.
* Memory pressure or inference errors do not crash the entire app.
* The user can return to the guide library if AI fails.
* Device compatibility is tested rather than inferred solely from RAM capacity.

## FR-03: Downloadable Model

Users must be able to download the AI model inside the application.

**Acceptance criteria**

* Download progress is visible.
* Failed downloads can be retried.
* File integrity is verified.
* Incomplete files cannot be activated as a valid model.
* Users can remove the model without losing household data or emergency guides.

## FR-04: Grounded AI Responses

AI responses about emergency safety must use approved retrieved sources.

**Acceptance criteria**

* The system retrieves relevant local documents.
* Source metadata is retained through retrieval and response generation.
* The interface displays the sources actually used.
* Unsupported questions receive a clear limitation message.
* The system does not fabricate citations or source dates.

## FR-05: Household Plan

Users can create and update a family preparedness plan offline.

**Acceptance criteria**

* Progress saves locally.
* The plan survives an ordinary app restart.
* Users can edit or delete their information.
* No account is needed.

## FR-06: Emergency Contacts

Users can save important contacts and initiate a call through a deliberate action.

**Acceptance criteria**

* Contacts remain available offline.
* The user must initiate a call.
* The interface never claims that a contact has been reached without confirmation from an actual calling workflow.

## FR-07: Language and Accessibility

Users can change language and text-size settings.

**Acceptance criteria**

* Settings persist after restarting the app.
* Enlarged text does not make essential actions inaccessible.
* Screen readers can identify key controls.
* Emergency guidance remains understandable at the largest supported text size.

## FR-08: Content Updates

The application can retrieve newer reviewed emergency content while online.

**Acceptance criteria**

* Each content pack has a version and release date.
* Applicable sources and review information are retained.
* Updates are verified before activation.
* Users are told when locally stored information may be outdated.
* The app does not claim that downloaded information is current merely because the app is online.

---

# 7. Technical Architecture

## 7.1 Android Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose
* **Architecture:** MVVM with clear domain and data layers
* **Local database:** Room / SQLite
* **Offline text search:** SQLite FTS5, where supported by the selected SQLite build
* **Preferences:** DataStore
* **Background work:** WorkManager for suitable deferrable tasks
* **Downloads:** A dedicated resumable download manager with foreground progress when appropriate
* **On-device inference:** Evaluate Google LiteRT-LM and the exact supported model artifacts before locking the implementation
* **Text-to-speech:** Android TextToSpeech where available

The selected inference runtime, model artifact, license, supported Android versions, and hardware requirements must be confirmed during a technical proof of concept.

## 7.2 AI Model Strategy

Prototype candidates:

* Gemma 3 1B IT.
* Qwen3-0.6B as a smaller fallback candidate.

These are candidates for testing, not a guarantee of compatibility or sufficient emergency-answer quality. Verify current distribution formats, licenses, quantization, tokenizer support, runtime compatibility, and memory requirements.

The app should initially favor a small, instruction-tuned model that can run on the target devices, with a strict local retrieval layer and a reviewed safety test set.

Do not assume a model will run reliably on every phone with 4 GB RAM. Test actual devices, available memory, inference latency, heat, and battery usage.

## 7.3 Retrieval-Augmented Generation (RAG)

1. Prepare approved emergency documents.
2. Divide documents into meaningful sections and chunks.
3. Store each chunk with its title, category, source, version, and review metadata.
4. Index the content locally.
5. Search for relevant passages when the user asks a question.
6. Provide retrieved passages to the model.
7. Generate an answer within a controlled prompt.
8. Display source references using the original stored metadata.

Start with local keyword retrieval. Add local embeddings only if testing demonstrates meaningful improvement and the extra storage and memory cost is justified.

## 7.4 Local Storage

Store the model and private household data in app-private storage.

Requirements:

* Do not use temporary cache storage as the permanent model location.
* Use partial-download files and checksum validation.
* Activate completed files atomically where supported.
* Keep user data separate from model files and content packs.
* Avoid logging personal emergency conversations by default.
* Define a safe cleanup and recovery procedure.

## 7.5 Optional Backend

The MVP does not require a backend for its core offline features.

An optional lightweight service can host:

* Model manifests.
* Model downloads.
* Verified emergency content packs.
* Content version information.
* Public feedback submissions, if implemented with consent.

The backend must not become a runtime dependency for local AI inference or access to already downloaded guides.

---

# 8. Emergency Content Governance and Safety

## 8.1 Source Approval

Emergency content must be reviewed against authoritative and qualified sources relevant to the Philippines. Candidate sources include:

* DOST-PHIVOLCS for earthquake and volcanic hazard information.
* PAGASA for weather and tropical cyclone information.
* Office of Civil Defense and applicable disaster risk reduction and management offices.
* Bureau of Fire Protection for fire safety.
* Philippine Red Cross and other appropriately qualified organizations for reviewed preparedness and first-aid education.
* Relevant Department of Health materials for health guidance.

The inclusion of an organization in this list does not imply endorsement of RescueDesk AI.

## 8.2 Content Metadata

Every approved content item must include, where applicable:

* Content ID.
* Title and category.
* Original source.
* Source URL or document reference.
* Publication or retrieval date.
* Reviewer and approval status.
* Last-reviewed date.
* Version.
* Applicable geographic scope.
* Language.
* Next review date, where defined.

## 8.3 Information Freshness

Clearly distinguish:

* **Saved guidance:** Reviewed content stored on the device.
* **Update available:** A newer pack has been identified.
* **Current official information:** Only show this label when the app has a reliable basis to verify the source and freshness.
* **Unverified or unavailable:** The app cannot confirm the needed information.

An offline app cannot know whether a new warning, evacuation order, or road closure has been issued since the last successful update.

## 8.4 Emergency Response Limitations

The MVP must not claim to:

* Dispatch rescue teams.
* Track incidents in real time.
* Confirm that an evacuation center is currently open.
* Provide live weather information while offline.
* Guarantee the safety of a recommended route.
* Replace emergency services or qualified medical professionals.

A future live-alert feature must have its own data source, update rules, permissions, and failure handling.

---

# 9. Privacy and Security

* No mandatory user account.
* No advertising trackers in the core MVP.
* No automatic upload of household contacts.
* No cloud processing of AI questions in the offline mode.
* HTTPS for online content downloads.
* Verify downloaded artifacts before use.
* Keep API keys and administrative credentials out of the Android application.
* Minimize diagnostic logs.
* Do not include household details or sensitive chat content in crash reports.
* Explain any optional data sharing in plain language.
* Allow users to delete locally stored personal data.
* Establish a clear policy for any future feedback or analytics collection.

Because local storage is not automatically safe against every form of device compromise, sensitive-data protection and Android backup behavior must be reviewed before release.

---

# 10. Performance and Compatibility

## 10.1 Initial Device Targets

* Android phones with 4 GB RAM: compatibility testing and constrained-mode support.
* Android phones with 6 GB RAM or more: primary prototype target.
* Sufficient free storage for the selected model, emergency packs, and temporary update files.
* Exact minimum Android OS version to be decided after runtime and library compatibility testing.

## 10.2 Performance Goals

These are engineering targets to validate, not guaranteed performance:

* Home screen should appear quickly on supported devices.
* Download progress should remain responsive.
* The emergency guide library should open without waiting for AI initialization.
* AI loading and response time should be measured on representative low-, mid-, and higher-tier phones.
* The app should recover gracefully from low memory, interrupted downloads, and inference errors.
* Long AI responses should not freeze the interface.
* The app should avoid excessive heat and battery drain during repeated inference.

Do not make a public performance promise until the target device test suite has passed.

---

# 11. MVP Scope

## Included in MVP

* Android application.
* Filipino and English interface.
* Accessibility settings and scalable text.
* Offline emergency guide library.
* Local guide search.
* Downloadable AI model.
* On-device AI chat.
* Grounded responses with source references.
* Family emergency plan.
* Saved emergency contacts.
* Go-bag checklist.
* Offline status and model manager.
* Reviewed content packs and update handling.
* Privacy and data deletion settings.
* Safety and usability testing.

## Excluded from MVP

* Live rescue dispatch.
* Automatic SOS notifications.
* Real-time location tracking.
* Live incident maps.
* Social feeds and community reporting.
* AI medical diagnosis.
* AI-generated evacuation orders.
* Mandatory accounts.
* Paid subscriptions for essential guidance.
* iOS release.
* Offline voice conversation.
* Complex LGU administration dashboards.

These features may be considered later only after their data, operational, privacy, and safety requirements are established.

---

# 12. Testing Plan

## 12.1 UI and Accessibility Tests

Test with:

* Young adult users.
* Parents and household organizers.
* Senior citizens.
* Users unfamiliar with AI applications.
* Users who rely on larger text or screen readers.

Tasks:

* Find a flood guide.
* Open an earthquake guide.
* Locate emergency contacts.
* Complete a go-bag checklist.
* Change the text size.
* Determine whether the AI model is installed.

Record task completion, errors, time to complete, and whether assistance was required.

## 12.2 Offline Tests

* Fresh install with no network.
* Open all previously installed guides in airplane mode.
* Search local content.
* Restart the app while offline.
* Open the AI assistant with a valid installed model.
* Open the AI assistant without a model.
* Confirm saved household data remains available.
* Confirm no essential screen waits indefinitely for a server.

## 12.3 Download Tests

* Pause and resume a download.
* Interrupt a download.
* Retry after a network failure.
* Reject a checksum mismatch.
* Handle insufficient storage.
* Update a model without deleting household data.
* Recover from an interrupted update.

## 12.4 AI Safety Tests

Create a reviewed test suite containing:

* Common preparedness questions.
* Filipino and English questions.
* Misspellings and informal wording.
* Questions not covered by the approved library.
* Questions requesting current weather or warnings while offline.
* Dangerous misconceptions.
* Conflicting or incomplete source material.
* Medical questions requiring professional attention.

Measure grounding, source accuracy, unsupported claims, unsafe instructions, and appropriate uncertainty. Critical unsafe outputs must block release until addressed.

## 12.5 Pilot Release Gate

Before broader distribution:

* All critical offline scenarios pass.
* No unresolved critical safety defects remain.
* The selected model and runtime pass device compatibility tests.
* Content reviewers approve the initial emergency pack.
* Accessibility tests are completed.
* Privacy and security checks are completed.
* Users can reach essential guides without AI.

---

# 13. Development Roadmap

## Phase 1 — Discovery and Proof of Concept

**Estimated duration:** 1 week

* Confirm model/runtime compatibility.
* Test inference on representative Android devices.
* Prepare the first reviewed emergency content pack.
* Validate offline storage and search.

**Deliverable:** A working on-device AI proof of concept and a verified guide library.

## Phase 2 — UI and Core Application

**Estimated duration:** 1–2 weeks

* Implement design system.
* Build onboarding and Home.
* Build emergency guide library.
* Build emergency guide detail screens.
* Add accessibility and language settings.

**Deliverable:** Usable Android application without requiring the AI model.

## Phase 3 — Offline AI and Downloads

**Estimated duration:** 1–2 weeks

* Add model download management.
* Add local inference.
* Add retrieval and source references.
* Implement offline and failure states.

**Deliverable:** A working local AI assistant using approved content.

## Phase 4 — Household Features

**Estimated duration:** 1 week

* Family emergency plan.
* Saved contacts.
* Go-bag checklist.
* Local persistence and deletion settings.

**Deliverable:** Functional household preparedness tools.

## Phase 5 — Safety, Accessibility, and Field Testing

**Estimated duration:** 1–2 weeks

* Device compatibility testing.
* AI safety evaluation.
* Senior-friendly usability testing.
* Offline recovery tests.
* Content review and pilot preparation.

**Deliverable:** Pilot-ready build.

**Overall initial estimate:** Approximately 6–8 weeks for one experienced developer, assuming timely content review, available hardware, and no major model compatibility issues. This is a planning estimate, not a delivery guarantee.

---

# 14. Proposed Project Deliverables

1. Product and UI/UX specification.
2. Android application source code.
3. Reusable UI component system.
4. Offline emergency guide pack.
5. On-device AI integration.
6. Model and content download manager.
7. Family preparedness tools.
8. Safety and privacy documentation.
9. Device and offline test reports.
10. Pilot deployment guide.
11. User instructions in Filipino and English.
12. Content update and review process.

---

# 15. Final Product Recommendation

Build RescueDesk AI as a **preparedness application first and an AI assistant second**.

The user should always be able to:

* Open a relevant emergency guide without chatting with AI.
* Read the instructions comfortably.
* Save a family plan without creating an account.
* Understand whether information is stored offline.
* Know when information may be outdated.
* Use the application when the AI model is unavailable.

The interface should be tested with real senior citizens and first-time smartphone users before release. Their feedback should directly influence the final button sizes, wording, navigation, and screen layout.

**The MVP is successful when a person under stress can find and understand an appropriate, reviewed safety guide—not merely when the AI can generate a convincing answer.**
