package com.github.yuriisurzhykov.kevent.statemachine.exceptions

/**
 *  Indicates whether state machine is initialized already.
 * */
class StateMachineInitializedException(smName: String) :
    IllegalStateException("State Machine $smName is already initialized!")