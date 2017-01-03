package com.se.api.data;

public class ErrorCode {
	public static int Correct = 0;
	
	public static int NotSystemManager = 1;  		//User's role isn't correct, not SystemManager/ ProjectManager/ Member
	public static int NotProjectManager = 2;  		//User's role isn't correct, not SystemManager/ ProjectManager/ Member
	public static int UserNull = 3;							//User isn't exist
	public static int CustomerNull = 4;				//Customer isn't exist
	public static int PersonInChargeNull = 5;	//PersonalInCharge isn't exist
	public static int ProjectNull = 6;						//Project isn't exist
	public static int IssueNull = 7;							//Issue isn't exist
	public static int NotMember = 8;					//User isn't member
	public static int UserIsInProject = 9;				//User is invited or joined in project
	public static int UserIsNotInProject = 10;	//User isn't invited or joined in project
	public static int PMCantRemove = 11;			//PM can't remove self member relational
	public static int SMCantInvited = 12;
	public static int UserNameRepeat = 13;
	public static int EmailFormatNotValid = 14;
	public static int UserNameNotExist = 15;
	public static int PasswordNotVaild = 16;
	public static int NotGeneralUser = 17;
	public static int IssueHasFinished = 18;
	public static int CantChangeProjectManager = 19;
	public static int CantSetIllegalRole = 20;
	public static int AlreadyReplaied = 21;
	public static int UserHaveProjectOrInvite=22;
}
