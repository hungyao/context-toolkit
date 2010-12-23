#***************** Component Subscription/Update Tables *****************

#Table of ComponentSubscription

CREATE TABLE ComponentSubscription (
	componentSubscriptionID INTEGER AUTO_INCREMENT, # componentSubscriptionID identifies ComponentSubscription
	componentID TEXT NOT NULL,
	subscriberID TEXT NOT NULL,
	`condition` TEXT,
	CONSTRAINT ComponentSubscriptionPrimaryKey PRIMARY KEY (componentSubscriptionID)

);

#Table of ComponentUpdate

CREATE TABLE ComponentUpdate (

	componentUpdateID INTEGER AUTO_INCREMENT, # componentUpdateID identifies ComponentUpdate
	componentID TEXT NOT NULL,
	updateTime TIMESTAMP NOT NULL,
	updateName TEXT NOT NULL,
	CONSTRAINT ComponentUpdatePrimaryKey PRIMARY KEY (componentUpdateID)

);

#Table of CUDestinations

CREATE TABLE CUDestinations (

	cuDestinationID INTEGER AUTO_INCREMENT,  # cuDestinationsID identifies CUDestinations
	componentUpdateID INTEGER,
	destinationComponentID TEXT NOT NULL, 
	success BOOLEAN, 
	CONSTRAINT CUDestinationsPrimaryKey PRIMARY KEY (cuDestinationID),
	CONSTRAINT CUDestinationsForeignKey FOREIGN KEY (componentUpdateID) REFERENCES ComponentUpdate    

);

#Table of CUAttributes 
#We may need to handle Attribute.STRUCT eventually...

CREATE TABLE CUAttributes (

	cuAttributeID INTEGER AUTO_INCREMENT, # cuAttributesID identifies CUAttributes
	cuDestinationID INTEGER, 
	attributeName TEXT NOT NULL, 
	attributeType TEXT NOT NULL, 
	attributeValueString TEXT, 
	attributeValueNumeric REAL, 
	constant BOOLEAN NOT NULL, 
	CONSTRAINT CUAttributesPrimaryKey PRIMARY KEY (cuAttributeID),
	CONSTRAINT CUAttributesForeignKey FOREIGN KEY (cuDestinationID) REFERENCES CUDestinations    

);


# ***************** Widget Registration Tables ***************** 

#Table of WidgetRegistration

CREATE TABLE WidgetRegistration (

	widgetRegistrationID INTEGER AUTO_INCREMENT, # widgetRegistrationID identifies WidgetRegistration
	widgetID TEXT NOT NULL, 
	registrationTime TIMESTAMP NOT NULL, 
	CONSTRAINT WidgetRegistrationPrimaryKey PRIMARY KEY (widgetRegistrationID)

);

#Table of WRAttributes
#We may need to handle Attribute.STRUCT eventually...

CREATE TABLE WRAttributes(
	
	wrAttributeID INTEGER AUTO_INCREMENT, # wrAttributesID identifies WRAttributes
	widgetRegistrationID INTEGER, 
	attributeName TEXT NOT NULL, 
	attributeType TEXT NOT NULL, 
	attributeValueString TEXT, 
	attributeValueNumeric REAL,
	constant BOOLEAN NOT NULL, 
	CONSTRAINT WRAttributesPrimaryKey PRIMARY KEY (wrAttributeID),
	CONSTRAINT WRAttributesForeignKey FOREIGN KEY (widgetRegistrationID) REFERENCES WidgetRegistration 

);

#Table of WRCallbacks
CREATE TABLE WRCallbacks (

	wrCallbackID INTEGER AUTO_INCREMENT, # wrCallbacksID identifies WRCallbacks
	widgetRegistrationID INTEGER, 
	callbackName TEXT NOT NULL, 
	CONSTRAINT WRCallbacksPrimaryKey PRIMARY KEY (wrCallbackID),
	CONSTRAINT WRCallbacksForeignKey FOREIGN KEY (widgetRegistrationID) REFERENCES WidgetRegistration 

);

#Table of WRServices
CREATE TABLE WRServices(

	wrServiceID INTEGER AUTO_INCREMENT, # wrServicesID identifies WRServices
	widgetRegistrationID INTEGER, 
	serviceName TEXT NOT NULL,
	CONSTRAINT WRServicesPrimaryKey PRIMARY KEY (wrServiceID),
	CONSTRAINT WRServicesForeignKey FOREIGN KEY (widgetRegistrationID) REFERENCES WidgetRegistration 

);

#Table of WRServiceFunctions
CREATE TABLE WRServiceFunctions(

	wrServiceFunctionID INTEGER AUTO_INCREMENT, # wrServiceFunctionsID identifies WRServiceFunctions
	wrServiceID INTEGER, 
	functionName TEXT,
	functionDescription TEXT, 
	CONSTRAINT WRServiceFunctionsPrimaryKey PRIMARY KEY (wrServiceFunctionID),
	CONSTRAINT WRServiceFunctionsForeignKey FOREIGN KEY (wrServiceID) REFERENCES WRServices

);

# *****************  Interpreter Registration Tables ***************** 

#Table of InterpreterRegistration

CREATE TABLE InterpreterRegistration (

	interpreterRegistrationID INTEGER AUTO_INCREMENT, # interpreterRegistrationID identifies InterpreterRegistration
	interpreterID TEXT, 
	registrationTime TIMESTAMP, 
	CONSTRAINT InterpreterRegistrationPrimaryKey PRIMARY KEY (interpreterRegistrationID)

);


#Table of IRAttributes
#We may need to handle Attribute.STRUCT eventually...

CREATE TABLE IRAttributes (

	irAttributeID INTEGER AUTO_INCREMENT, # irAttributesID identifies IRAttributes
	interpreterRegistrationID INTEGER, # interpreterRegistrationID partly identifies IRAttributes
	attributeName TEXT NOT NULL,
	InOrOut BOOLEAN, 
	attributeType TEXT, 
	attributeValueString TEXT, 
	attributeValueNumeric REAL, 
	CONSTRAINT IRAttributesPrimaryKey PRIMARY KEY (irAttributeID),
	CONSTRAINT IRAttributesForeignKey FOREIGN KEY (interpreterRegistrationID) REFERENCES InterpreterRegistration

);

# *****************  Enactor Registration Tables ***************** 

#Table of EnactorRegistration

CREATE TABLE EnactorRegistration ( 

	enactorRegistrationID INTEGER AUTO_INCREMENT, # enactorRegistrationID identifies EnactorRegistration
	enactorID TEXT NOT NULL, 
	registrationTime TIMESTAMP NOT NULL, 
	CONSTRAINT EnactorRegistrationPrimaryKey PRIMARY KEY (enactorRegistrationID) 

);

#Table of ERReferences

CREATE TABLE ERReferences ( 

	erReferenceID INTEGER AUTO_INCREMENT, #enactorReferenceID identifies ERReferences
	enactorRegistrationID INTEGER,
	descriptionQuery TEXT, 
	CONSTRAINT ERReferencesPrimaryKey PRIMARY KEY (erReferenceID), 
	CONSTRAINT ERReferencesForeignKey FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration

);

#Table of ERParameters

CREATE TABLE ERParameters( 

	erParameterID INTEGER AUTO_INCREMENT, # erParameters identifies erParametersID
	enactorRegistrationID INTEGER, 
	parameterName TEXT NOT NULL, 
	CONSTRAINT ERParametersPrimaryKey PRIMARY KEY (erParameterID), 
	CONSTRAINT ERParametersForeignKey FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration

);


#Table of ERActions

#CREATE TABLE ERActions(
#
#	enactorRegistrationID INTEGER AUTO_INCREMENT, # enactorRegistrationID identifies ERActions
#	CONSTRAINT ERActionsPrimaryKey PRIMARY KEY (enactorRegistrationID), 
#	CONSTRAINT ERActionsForeignKey FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration

#);


# *****************  Enactor Runtime Tables ***************** 

#Table of ComponentEvaluated

CREATE TABLE ComponentEvaluated( 

	componentEvaluatedID INTEGER AUTO_INCREMENT, # componentEvaluatedID identifies ComponentEvaluated
	enactorRegistrationID INTEGER, 
	erReferenceID INTEGER,
	componentDescriptionID TEXT NOT NULL,
	componentEvaluatedTime TIMESTAMP NOT NULL, 
	CONSTRAINT ComponentEvaluatedPrimaryKey PRIMARY KEY (componentEvaluatedID), 
	CONSTRAINT ComponentEvaluatedForeignKeyEnactorRegistration FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration,
	CONSTRAINT ComponentEvaluatedForeignKeyERReferences FOREIGN KEY (erReferenceID) REFERENCES ERReferences
	
);

#Table of ComponentAdded

CREATE TABLE ComponentAdded( 

	componentAddedID INTEGER AUTO_INCREMENT, # componentAddedID identifies ComponentAdded
	enactorRegistrationID INTEGER, 
	erReferenceID INTEGER,
	componentDescriptionID TEXT NOT NULL,
	componentAddedTime TIMESTAMP NOT NULL, 
	CONSTRAINT ComponentAddedPrimaryKey PRIMARY KEY (componentAddedID), 
	CONSTRAINT ComponentAddedForeignKeyEnactorRegistration FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration,
	CONSTRAINT ComponentAddedForeignKeyERReferences FOREIGN KEY (erReferenceID) REFERENCES ERReferences

);

#Table of CAParamAttributes

CREATE TABLE CAParamAttributes(

	caParamAttributeID INTEGER AUTO_INCREMENT, # caParamAttributesID identifies CAParamAttributes
	componentAddedID INTEGER, 
	attributeName TEXT, 
	attributeType TEXT, 
	attributeValueString TEXT, 
	attributeValueNumeric REAL,
	CONSTRAINT CAParamAttributesPrimaryKey PRIMARY KEY (caParamAttributeID),
	CONSTRAINT CAParamAttributesForeignKey FOREIGN KEY (componentAddedID) REFERENCES ComponentAdded 

);

#Table of ParameterValueChanged

CREATE TABLE ParameterValueChanged( 

	parameterValueChangedID INTEGER AUTO_INCREMENT, # parameterValueChangedID identifies ParameterValueChanged
	enactorRegistrationID INTEGER, 
	erParameterID INTEGER, 
	parameterValueString TEXT,
	parameterValueNumeric REAL,
	parameterValueChangedTime TIMESTAMP NOT NULL, 
	CONSTRAINT ParameterValueChangedPrimaryKey PRIMARY KEY (parameterValueChangedID),
	CONSTRAINT ParameterValueChangedForeignKeyEnactorRegistration FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration,
	CONSTRAINT ParameterValueChangedForeignKeyERParameters FOREIGN KEY (erParameterID) REFERENCES ERParameters
	
);

#Table of PVCParamAttributes

CREATE TABLE PVCParamAttributes(

	pvcParamAttributeID INTEGER AUTO_INCREMENT, # pvcParamAttributeID identifies PVCParamAttributes
	parameterValueChangedID INTEGER,
	attributeName TEXT, 
	attributeType TEXT, 
	attributeValueString TEXT,
	attributeValueNumeric REAL,
	CONSTRAINT PVCParamAttributesPrimaryKey PRIMARY KEY (pvcParamAttributeID),
	CONSTRAINT PVCParamAttributesForeignKey FOREIGN KEY (parameterValueChangedID) REFERENCES ParameterValueChanged 

);

# *****************  Service Execution Tables ***************** 

#Table of ServiceExecution

CREATE TABLE ServiceExecution (

	serviceExecutionID INTEGER AUTO_INCREMENT, # serviceExecutionID identifies ServiceExecution
	enactorRegistrationID INTEGER,
	componentAddedID INTEGER, 
	serviceName TEXT NOT NULL, 
	functionName TEXT NOT NULL, 
	executionTime TIMESTAMP NOT NULL,
	CONSTRAINT ServiceExecutionPrimaryKey PRIMARY KEY (serviceExecutionID),
	CONSTRAINT ServiceExecutionForeignKeyEnactorRegistration FOREIGN KEY (enactorRegistrationID) REFERENCES EnactorRegistration,
	CONSTRAINT ServiceExecutionForeignKeyComponentAdded FOREIGN KEY (componentAddedID) REFERENCES ComponentAdded
	
);


CREATE TABLE SEInputAttributes (

	seInputAttributeID INTEGER AUTO_INCREMENT, # seInputAttributeID identifies SEInputAttributes
	serviceExecutionID INTEGER, 
	attributeName TEXT, 
	attributeType TEXT, 
	attributeValueString TEXT, 
	attributeValueNumeric REAL,
	CONSTRAINT SEInputAttributesPrimaryKey PRIMARY KEY (seInputAttributeID),
	CONSTRAINT SEInputAttributesForeignKey FOREIGN KEY (serviceExecutionID) REFERENCES ServiceExecution
	
);
