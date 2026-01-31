package it.medcare.doc_repo.common.rest;

public final class CustomExceptions {

    private CustomExceptions() {}

    public static class CustomException extends RuntimeException {
    	
        public CustomException(String message) {
        	
            super(message);
        }
    }
}
