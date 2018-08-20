package org.openzen.zencode.shared;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;

public final class CompileException extends RuntimeException {
    public static CompileException internalError(String message) {
        return new CompileException(CodePosition.BUILTIN, CompileExceptionCode.INTERNAL_ERROR, message);
    }
    
    public final CodePosition position;
    public final CompileExceptionCode code;
    
    public CompileException(CodePosition position, CompileExceptionCode code, String message) {
        super(position.toString() + ": [" + code.toString() + "] " + message);
        this.position = position;
        this.code = code;
    }
    
    public CodePosition getPosition() {
        return position;
    }
    
    public CompileExceptionCode getCode() {
        return code;
    }
}
