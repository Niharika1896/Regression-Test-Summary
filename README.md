# DBprojects
A Java application to administer the real-time status of individual tests running in a test suite (comprising 35000) test cases.

**Purpose of the project:** Develop and application for real time analysis of test status, that also enables automatic comparison with the previous run of the test and has a feature to add/update test analysis comments, if any. 

**Test Structure:**
The tests follow a 4-level structure. The is a test suite (for 32-bit or 64-bit system), consisting of test categories(based on database features), which further consist of test units which are finally made up of inidividual test cases. 

**Functionalities included:**
1. View real time test status.
2. Update the backend database with the correct status of each individual test case.
3. Export just the failed test cases for in-depth analysis. 
4. Automatically compare the test results with the logs of the previous run to determine if a test has passed. Enable comaprison across multiple releases. 
5. Update the expected file/result of a test case. 
6. Give test summary of individual test cases, as well as aggregate of test cases (for example, test summary of a test suite, test category or test unit).
7. Record the test duration time by capturing the test start and finish timestamp for future time analysis. 

