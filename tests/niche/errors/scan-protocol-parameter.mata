unit amount = 10
protocol inspect(amount) { scan amount }
inspect(5) # expect runtime error: Undefined unit 'amount'.
