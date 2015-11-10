Dining philosophers problem using Akka with FSM.

// Clean and compile

sbt clean compile

// Run

sbt run

// Build Eclipse project

sbt eclipse:clean eclipse

// Build executable package

sbt stage universal:packageBin

// Run executable package

./target/universal/stage/bin/dining
