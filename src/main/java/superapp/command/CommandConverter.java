package superapp.command;



import org.springframework.stereotype.Component;

@Component
public class CommandConverter {

	public CommandEntity toEntity (MiniAppCommandBoundary boundary) {
		CommandEntity entity = new CommandEntity();
		
		entity.setCommandId(boundary.getCommandId().getSuperapp()+"@@"+boundary.getCommandId().getMiniapp()+"@@"+boundary.getCommandId().getId());
		entity.setCommand(boundary.getCommand());
		entity.setTargetObject(boundary.getTargetObject().getObjectId().getSuperapp()+"@@"+boundary.getTargetObject().getObjectId().getId());

		entity.setInvocationTimestamp(boundary.getInvocationTimestamp());
		entity.setInvokedBy(boundary.getInvokedBy().getUserId().getSuperapp()+"@@"+boundary.getInvokedBy().getUserId().getEmail());	

		entity.setCommandAttributes(boundary.getCommandAttributes());
		entity.setMiniAppName(boundary.getCommandId().getMiniapp());
		return entity;
	}
	
	public MiniAppCommandBoundary toBoundary(CommandEntity entity) {
		MiniAppCommandBoundary boundary = new MiniAppCommandBoundary();
		String[] commandId = convertFromEntityId(entity.getCommandId());
		String[] targetObject = convertFromEntityId(entity.getTargetObject());
		String[] invokedBy = convertFromEntityId(entity.getInvokedBy());
		boundary.setCommandId(new CommandIdBoundary(commandId[0],commandId[1],commandId[2]));
        boundary.setCommand(entity.getCommand());
        boundary.setTargetObject(new TargetObjectBoundary(targetObject[0],targetObject[1]));
        boundary.setInvocationTimestamp(entity.getInvocationTimestamp());
        boundary.setInvokedBy(new InvokedByBoundary(invokedBy[0],invokedBy[1]));
        boundary.setCommandAttributes(entity.getCommandAttributes());
        return boundary;
	}
	
	
	public String[] convertFromEntityId(String entityId) {
		String[] str1 = entityId.split("@@");
		return str1;
	}


}
