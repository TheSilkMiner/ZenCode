package org.openzen.zencode.shared;

public final class CompileException extends Exception {
    public static CompileException internalError(String message) {
        return new CompileException(CodePosition.BUILTIN, CompileExceptionCode.INTERNAL_ERROR, message);
    }
    
    public final CodePosition position;
    public final CompileExceptionCode code;
	public final String message;
    
    public CompileException(CodePosition position, CompileExceptionCode code, String message) {
        super(position.toString() + ": [" + code.toString() + "] " + message);
		
        this.position = position;
        this.code = code;
		this.message = message;
    }
    
    public CodePosition getPosition() {
        return position;
    }
    
    public CompileExceptionCode getCode() {
        return code;
    }
}
