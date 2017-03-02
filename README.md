# IP result parser

IP result parser is a utility to convert pdf formatted IP University result to CSV.

# How to run

mvn clean package

java -jar ./target/ipresultparser-1.0.jar path_to_input_pdf path_of_result_directory

# Output

Output will be multiple files in  "path_of_result_directory"

### File names will be in the following format:
semester_instituteCode_instituteName.csv

### Example name:
02_244_INSTITUTE_OF_INNOVATION_IN_TECHNOLOGY.csv

### Columns will be all the subjects
Institute, name, semester, business_organization_minor, business_organization_major, business_organization_total, business_economics-ii_minor, business_economics-ii_major, business_economics-ii_total, quantitative_techniques_&_operations_research_in_management_minor, quantitative_techniques_&_operations_research_in_management_major, quantitative_techniques_&_operations_research_in_management_total, data_base_management_system_minor	data_base_management_system_major, data_base_management_system_total, cost_accounting_minor, cost_accounting_major, cost_accounting_total, personality_development_and_communication_skills-ii_minor, personality_development_and_communication_skills-ii_major, personality_development_and_communication_skills-ii_total, dbms_lab_minor, dbms_lab_major, dbms_lab_total

# What if a specific IP University result does not work?
Create an issue in the github repo. And share the link of pdf. If I have time I will try my best to fix the issue.

# Thanks
You are welcome
