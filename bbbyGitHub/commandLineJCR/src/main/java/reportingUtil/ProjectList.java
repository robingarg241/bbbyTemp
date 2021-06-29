package reportingUtil;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlRootElement(name="Projects")
public class ProjectList {

	@XmlElement(name="Project")
	public List<ProjectBean> getPlist() {
		return plist;
	}

	public void add(ProjectBean p){
		plist.add(p);
	}
	
	public void setPlist(List<ProjectBean> plist) {
		this.plist = plist;
	}

	private List<ProjectBean> plist = new ArrayList<ProjectBean>();

}
