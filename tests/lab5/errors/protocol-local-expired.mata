{
    protocol local() { transmit 3 }
}
local() # expect runtime error: Undefined variable 'local'.
