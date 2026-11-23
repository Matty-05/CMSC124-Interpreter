if (active) {
    unit local = 1
}
report local # expect runtime error: Undefined variable 'local'.
