/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import org.openzen.zenscript.formattershared.ExpressionString;

/**
 * @author Hoofdgebruiker
 */
public interface JavaSourceBuiltinBinary {
	ExpressionString compile(ExpressionString left, ExpressionString right);
}
