Human AI Net - the Human and Artificial Intelligence Network 

2017-6-3 ABOUT CODE SOON TO COME

humanAiNet is an incomplete mad-scientists dream of turning the world into a big brain. Its parts have been through many redesigns, each time becoming more minimalist (so expect lots of code changes from old versions), and eventually was decided to create a progLang and virtualMachine and port the important parts into that. So theres half-working parts laying around like you'd expect in a mad-scientists lab. Its so advanced a goal nothing less than hardcore minimalism can work, but I think I'm close to a stable core, and here's the plan for that...

The purpose of humanAiNet has not changed: to network minds together like a bigger mind by accessing subconscious psychology of many people in massively multiplayer games and put Human intelligence mixed with AI into the Internet and eachothers minds, through the slow bandwidth of mouse and keyboard or faster bandwidth of any bidirectional vector stream interface people may hook in later. Its also about the tools needed to do that including scalable software infrastructure and AI math, all minimalist, surprisingly simple for the advanced things it will do.

The game will make use of the property of less than perfectly rational minds of believing x is better than y is better than z is better than x, for any x y and z or similar paradoxical loops, which is why games feel fun despite doing the same few things over and over. humanAiNet will include tools to design and automatically tune such games to predict human pleasure in each possible event and find cycles of positive total pleasure that lead back to mostly where you started, so going around that main gameplay logic people will have lots of fun despite not accomplishing anything useful, except people can hook their subconscious minds into useful things (like statistical prediction, networking minds together to do bigger things, etc) after AIs and peoples motivations are understood and looped together into fun games. A variety of games have been tried including smartblob (n dimensional vector controlling radius at each angle to reshape blobs), audivolv (evolution of music tools), sum of person and neuralnet moving in 1 dimension together, but as it turns out we need to abstract that to functions that observe game state including mouse and keyboard inputs and return a wrapped int[][] of pixel colors for that small fraction of a second (to be painted by BufferedImage), since we need flexibility to redesign the games while the system runs instead of committing to just 1 kind of game. AI prediction is generally useful for building strange kinds of gameplay, and the x<y<z<x thing is very openended research paths.

The new progLang and virtualMachine will have these properties:
* Pure functional, immutable, stateless.
* An object is either a float64 or a map of object to object, where list is a map whose keys are all float64 integers 0 to size-1.
* Efficient number crunching
* ControlFlow includes s and k like in unlambda s=(Lx.Ly.Lz.((xz)(yz)) k=(Lx.Ly.x), sandboxed code strings of other languages (sandboxed to only modify primitive arrays before other funcs have seen them effectively making them immutable), etupmoc (TODO explain), and tracking and limiting pluggable models of memory and compute cycles (including econacyc and recursiveExpireTime costing mem*time and mem).
* log time and memory for forkModify including binary concat, sublist, and submap, while lazyEval-secureHash homomorphic dedup regardless of order of tree rotations (different internal tree structures but same map contents).
* Positive opcodes are strict, and negative opcodes nonstrict. Opcode 0 is noOp.
* All opcodes are immutable functions of 1 param, with currying, and are represented by a certain float64, and some of them are used like [[opcode curriedParams] param], where curriedParams is an arbitrary structure specific to that opcode.
* Like iota language, all possible softwares are derivable from combinations of a single opcode, the inc (increment) opcode, since inc(inc) gives the next opcode, and inc(inc(inc)) gives the one after that, and soon you get to the plus and multiply opcodes so can reach the others faster. Eventually you reach opcodes about maps so can create more complex objects. The s opcode (like in unlambda) creates a binary forest of functions that copy the param down the forest and you use skk (s curry k curry k) as identityFunc to get that param or use k to quote. Optimizations will include caching those redundant s calls and sometimes reverseTailCalling to wrap them into arrays and loops as java code compiled by javassist (if code passes sandbox, which it will since its generated) else run the slower interpreted way.

I'll try to organize and publish enough of this stuff in a way other people can understand and maybe contribute to, but its a big effort to track down all these parts that dont fit together yet while keeping it minimalist. I'm hoping this will be the last redesign of the core.




OLD...

Requires Java 1.7+

Ben F Rayfield offers HumanAiNet opensource GNU GPL 2+, while some parts individually offer other licenses.

Other urls:
http://sourceforge.net/projects/humanainet
Github name humanainet

We are going to teach eachother how to build AI and new kinds of game objects in this network using intuitive dragAndDrop. Its going to be a space for experimenting with fun and useful tools in new ways. Version 0.8.0 has some advanced components that will be working soon. The plan is a massively multiplayer space where we design, evolve, and play with game objects and do AI research together which controls those game objects along with directly playing the games. The main data format is, from xorlisp which is also in progress, immutable binary forest nodes, so if millions of people build that together nobody can damage or change anyone else's data since its all constant. You dont change variables. You create new data that points at existing constant data, as deep as you need it. I have mindmap lists, definitions, and 2 editable properties working that way with 2 kinds of event listeners that work locally. Its not a networked system yet, but the datastructs are ready to scale with it.
