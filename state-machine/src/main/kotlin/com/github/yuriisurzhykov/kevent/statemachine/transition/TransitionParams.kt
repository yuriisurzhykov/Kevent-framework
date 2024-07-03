package com.github.yuriisurzhykov.kevent.statemachine.transition

/**
 * This interface provides a way to pass parameters between different states of a state machine.
 * When an event comes to a state machine we may need to pass an extra parameter(-s) to the next
 * state in order to keep code clean and maintainable. This interfaces extends the abilities of
 * a state machine code.
 *
 * Current implementation of it is just an [Empty] object that has nothing in it. Empty
 * implementation allows to make the environment to be a feature-read if needed in the future.
 * */
interface TransitionParams {

    object Empty : TransitionParams
}