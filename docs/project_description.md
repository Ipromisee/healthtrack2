##📄 PROJECT: HealthTrack Personal Wellness Platform---

###**📋 Purpose of this Project*** Analyze, design, implement, and document a database system application.


* You will use the methodology for database development learned in class.


* The system must be implemented on a DBMS with any language as a host-language for the application.


* The system can be implemented as a desktop, web, or mobile application.


* The application must have a Graphical User Interface (GUI) that includes basic functionality.



---

###**💡 HealthTrack Personal Wellness Platform Specifications**The specifications below are a guide to get you started. As the designer, you must analyze and decide what other necessary details or features should be specified. Thus, individual group implementations will differ in design and implementation styles. Every group must clearly mention any assumed specifications in their report.

####**Data and Functional Requirements**HealthTrack is a personal health and wellness management platform, similar to MyChart or Fitbit. It enables individuals to track health metrics, manage medical appointments, and engage in wellness activities.

* **Users/Accounts:**
* Users sign up with their name, a unique Health ID (similar to an SSN), an email address, and a phone number.


* Only **one phone number** can be recorded per account.


* An account can be associated with **multiple email addresses**.


* Contact information (email, phone) should be verified before use for critical functions (like receiving alerts).


* The system should record both verified and non-verified contact information. (The verification process is irrelevant for the E/R schema design ).




* **Healthcare Providers:**
* Users can link **multiple healthcare providers** (doctors, specialists, therapists) to their account.


* One linked provider must be defined as the user's **primary care physician**.


* Providers are identified by a **unique medical license number**.


* Providers must be verified (e.g., against a registry) before being officially linked.


* Users must be able to unlink a provider.


* The system should record both verified and non-verified providers. (The verification process is irrelevant for the E/R schema design ).




* **Family Groups:**
* Two or more users can be associated as a **"Family Group"**.


* This allows one user (e.g., a parent) to help manage the health profile of another (e.g., a child), with appropriate permissions.





####**Supported Actions (Functional Requirements)**HealthTrack supports two forms of actions:

1. 
**Book an Appointment with a provider**.


2. 
**Create a Wellness Challenge for other users**.



* **Book an Appointment:**
* Requires specifying: provider's license number or verified email, date and time, type of consultation (e.g., In-Person, Virtual), and an optional memo (for symptoms/questions).


* Every appointment transaction must have a **unique Id**.


* Appointments can be **cancelled** up to 24 hours before the scheduled time.


* Cancelled appointments and the reason for cancellation (e.g., "Patient Rescheduled," "Provider Unavailable") should be recorded in the database.




* **Create a Wellness Challenge:**
* A user invites others by specifying their email addresses or phone numbers, the challenge goal (e.g., "Walk 100 miles in a month"), and start/end dates.


* The use case is typically to "Start a Fitness Goal," shared among friends and family.


* Every challenge has a **unique Id**.


* The system should specify which users are participating and track their progress.




* **Invitations:**
* An **invitation** (to a challenge or to view shared data) sent to an unassociated/unverified email or phone number is considered an invitation to a **new user**.


* The recipient can accept by signing up within **15 days**. After 15 days, the invitation is cancelled.


* For every invitation, the date and time it was initiated and the date and time it was completed (or expired) should be recorded.




* **Reporting and Search:**
* Users' health data and history should be organized in **monthly summary reports**.


* 
**Search functionality** should be provided (e.g., search for a provider, search for an appointment by date, search for total number of steps walked in a given month, etc.).





---

###**⚠️ Important Design Notes*** You can make further assumptions, but they must **not contradict** the assumptions described above, and they must be **clearly stated in your report**.


* The description contains both database requirements and functional requirements (actions related to the database).


* In designing your database schemas, ensure you add constructs needed to store all required data.


* The functional requirements will be implemented at the application level.



---

Would you like me to help you identify the core entities and relationships for the E/R schema design?