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
	
	
	public static int UserNameRepeat =9 ;
	public static int EmailFormatNotValid =10;
	public static int UserNameNotExist =11;
	public static int PasswordNotVaild =12;
	public static int NotGeneralUser =13;
}
