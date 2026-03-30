# CheckX

> Your terminal companion for habits & focus

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Terminal-based habit tracker built with pure Java. No frameworks, no bloat - just clean code and beautiful CLI.

```
 ██████ ██   ██ ███████  ██████ ██   ██ ██   ██
██      ██   ██ ██      ██      ██  ██   ██ ██ 
██      ███████ █████   ██      █████     ███  
██      ██   ██ ██      ██      ██  ██   ██ ██ 
 ██████ ██   ██ ███████  ██████ ██   ██ ██   ██
```

## ✨ Features

- 🐚 **Interactive shell** - Unix-style command interface (like bash/zsh)
- 🎯 **Simple habit tracking** - Add, complete, and track your daily habits
- 🔥 **Streak system** - Build momentum with daily streaks
- 📊 **Statistics** - View your progress and completion rates
- 🎨 **Beautiful CLI** - Colorful terminal interface with pure ANSI codes
- 💾 **Local storage** - JSON-based persistence in `~/.checkx/`
- ⚡ **Fast & lightweight** - Pure Java, no heavy frameworks
- 🔧 **Dual mode** - Interactive shell OR quick CLI commands
- 💬 **Command aliases** - Multiple ways to run the same command

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/checkx.git
cd checkx
```

2. **Build the project**
```bash
mvn clean package
```

This creates `target/checkx.jar`

3. **Create an alias** (optional but recommended)

Add to your `~/.bashrc` or `~/.zshrc`:
```bash
alias checkx='java -jar /path/to/checkx/target/checkx.jar'
```

Or create a shorter alias:
```bash
alias cx='java -jar /path/to/checkx/target/checkx.jar'
```

Reload your shell:
```bash
source ~/.bashrc  # or source ~/.zshrc
```

### Alternative: Make it executable

```bash
# Copy JAR to local bin
mkdir -p ~/.local/bin
cp target/checkx.jar ~/.local/bin/

# Create wrapper script
cat > ~/.local/bin/checkx << 'EOF'
#!/bin/bash
java -jar ~/.local/bin/checkx.jar "$@"
EOF

chmod +x ~/.local/bin/checkx

# Add to PATH if needed
export PATH="$HOME/.local/bin:$PATH"
```

## 📖 Usage

CheckX supports **two modes**:

### 🐚 Interactive Shell (Recommended)

Simply run without arguments to enter interactive shell:

```bash
checkx
```

Or:
```bash
java -jar target/checkx.jar
```

This opens a Unix-style command shell:
```
 ██████ ██   ██ ███████  ██████ ██   ██ ██   ██
██      ██   ██ ██      ██      ██  ██   ██ ██ 
██      ███████ █████   ██      █████     ███  
██      ██   ██ ██      ██      ██  ██   ██ ██ 
 ██████ ██   ██ ███████  ██████ ██   ██ ██   ██
     Your terminal companion for habits & focus

  Type 'help' for available commands

$ daily
$ done exercise
$ stats
$ help
$ exit
```

**Available commands:**
- `daily`, `today` - Show today's habits
- `done [name]` - Complete a habit
- `add [name]` - Add new habit
- `stats` - Show statistics
- `stats daily` - Today's statistics
- `stats all` - All-time statistics
- `list`, `habits` - List all habits
- `delete [name]` - Delete a habit
- `help` - Show all commands
- `clear` - Clear screen
- `exit`, `quit` - Exit

### ⚡ CLI Mode (Quick Commands)

Use individual commands for scripting or quick actions:

```bash
checkx daily          # View today's habits
checkx done exercise  # Complete a habit
checkx add "Read"     # Add a habit
checkx stats          # View statistics
checkx list           # List all habits
```

## 🎨 Terminal Recommendations

For the best experience, use a modern terminal with Unicode and emoji support:

**Recommended fonts:**
- JetBrains Mono
- Fira Code
- Cascadia Code
- SF Mono

**Tested terminals:**
- ✅ iTerm2 (macOS)
- ✅ Windows Terminal
- ✅ Alacritty
- ✅ Kitty
- ⚠️ Basic terminals may show broken characters

## 🏗️ Architecture

```
checkx/
├── domain/              # Core business logic
│   ├── Habit.java       # Habit entity with streak logic
│   ├── Stats.java       # Statistics calculator
│   └── HabitRepository.java
├── infrastructure/      # External concerns
│   ├── JsonHabitRepository.java
│   └── LocalDateAdapter.java
├── commands/            # CLI commands (Picocli)
│   ├── CheckXCommand.java
│   ├── DailyCommand.java
│   ├── DoneCommand.java
│   ├── AddCommand.java
│   ├── StatsCommand.java
│   └── ListCommand.java
└── ui/                  # Presentation
    └── ConsoleColors.java
```

**Design principles:**
- Clean architecture / Hexagonal architecture
- Domain-driven design
- Pure Java (no Spring or heavy frameworks)
- Simple JSON persistence

## 🛠️ Tech Stack

- **Java 17** - Modern Java with records and pattern matching
- **Maven** - Dependency management
- **Picocli** - CLI framework
- **Gson** - JSON serialization
- **Pure ANSI codes** - No external color libraries (100% Java)

## 📦 Building

```bash
# Compile
mvn compile

# Run tests (when added)
mvn test

# Package as JAR
mvn package

# Clean build
mvn clean package
```

## 🔮 Roadmap

### Version 1.1
- [ ] Edit habit name/icon
- [ ] Delete habits
- [ ] Custom habit icons

### Version 2.0
- [ ] Pomodoro timer integration
- [ ] Weekly/monthly reports
- [ ] Export data (CSV/JSON)

### Version 3.0
- [ ] Multiple routines (Morning/Evening)
- [ ] Habit categories
- [ ] ASCII graphs for trends

### Future
- [ ] GraalVM native binary
- [ ] Cloud sync (optional)
- [ ] Multiple themes

## 🤝 Contributing

Contributions welcome! This is a learning project, so feel free to:
- Report bugs
- Suggest features
- Submit pull requests
- Improve documentation

## 📄 License

MIT License - feel free to use this project however you want!

## 🙏 Acknowledgments

- Inspired by terminal-based productivity tools
- Built with ❤️ and ☕

---

**Made with Java and passion** 🚀
