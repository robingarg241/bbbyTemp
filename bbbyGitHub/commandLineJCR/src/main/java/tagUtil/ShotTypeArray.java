package tagUtil;

import java.util.HashMap;

public class ShotTypeArray {
	
	public static HashMap getShotTypeArray(){
		
		HashMap<String, String> shotTypeArray = new HashMap<String, String>();
		
		shotTypeArray.put("Back","belkin:shot_type/back");
		shotTypeArray.put("Hero", "belkin:shot_type/hero");
		shotTypeArray.put("Left", "belkin:shot_type/left");
		shotTypeArray.put("Right", "belkin:shot_type/right");
		shotTypeArray.put("Detail View", "belkin:shot_type/detail_view");
		shotTypeArray.put("Front", "belkin:shot_type/front");
		shotTypeArray.put("Top", "belkin:shot_type/top");
        shotTypeArray.put("In Use", "belkin:shot_type/in_use");
		
		return shotTypeArray;
		
	}

}
