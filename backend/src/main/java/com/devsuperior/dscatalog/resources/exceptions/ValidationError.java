package com.devsuperior.dscatalog.resources.exceptions;

import java.util.ArrayList;
import java.util.List;

public class ValidationError extends StandardError{
	private static final long serialVersionUID = 1L;
	
	// class FieldMessage + atributos e metodos do StandardError(Herança).
	private List<FieldMessage> erros = new ArrayList<>();

	// Get 
	public List<FieldMessage> getErros() {
		return erros;
	}

	public void addError (String fieldName, String message) {
		erros.add(new FieldMessage(fieldName, message));
		
	}
}
