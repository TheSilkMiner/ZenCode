/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.storage;

/**
 *
 * @author Hoofdgebruiker
 */
public class SharedStorageType implements StorageType {
	public static final SharedStorageType INSTANCE = new SharedStorageType();
	
	private SharedStorageType() {}
}