package superapp.command;

import java.util.Date;
import java.util.Map;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "COMMAND_TBL")
public class CommandEntity {
	@Id private String commandId;
	private String command;
	private String targetObject;
	

	// String miniAppname
	@Temporal(TemporalType.TIMESTAMP)
	private Date invocationTimestamp;

	private String invokedBy;
	private String miniAppName;

	@Lob
	@Convert(converter = CoverterToCommandMapFromString.class)
	private Map<String, Object> commandAttributes;

	public CommandEntity() {
		super();
	}

	public String getCommandId() {
		return commandId;
	}

	public void setCommandId(String commandId) {
		this.commandId = commandId;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(String targetObject) {
		this.targetObject = targetObject;
	}

	public Date getInvocationTimestamp() {
		return invocationTimestamp;
	}
	public void setInvocationTimestamp(Date invocationTimeStamp) {
		this.invocationTimestamp = invocationTimeStamp;
	}

	public Map<String, Object> getCommandAttributes() {
		return commandAttributes;
	}

	public void setCommandAttributes(Map<String,Object> map) {
		this.commandAttributes = map;
	}



	public String getInvokedBy() {
		return invokedBy;
	}

	public void setInvokedBy(String invokedBy) {
		this.invokedBy = invokedBy;
	}

	public String getMiniAppName() {
		return miniAppName;
	}

	public void setMiniAppName(String miniAppName) {
		this.miniAppName = miniAppName;
	}

	@Override
	public String toString() {
		return "CommandEntity [commandId=" + commandId + ", command=" + command + ", targetObject=" + targetObject
				+ ", invocationTimeStamp=" + invocationTimestamp + ", invokedBy=" + invokedBy + ", miniAppName="
				+ miniAppName + ", commandAttributes=" + commandAttributes + "]";
	}

	

	

}
