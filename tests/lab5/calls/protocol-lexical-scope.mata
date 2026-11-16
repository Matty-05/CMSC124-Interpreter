unit greeting = "hello"
protocol greet() { report greeting }
protocol rude() {
    unit greeting = "go away"
    greet()
}
rude() # expect: hello
