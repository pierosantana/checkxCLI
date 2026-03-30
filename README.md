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

## Features

- Interactive shell with weekly calendar and progress bar
- Streaks, statistics, habit comments and icon selector
- Edit, undo, delete and partial name search
- Local JSON storage, pure Java, no heavy frameworks

## Quick Start

Requires **Java 17+** and **Maven 3.6+**.

```bash
git clone https://github.com/pierosantana/checkxCLI.git
cd checkxCLI
mvn clean package
java -jar target/checkx.jar
```

Optional: add an alias to your `~/.zshrc` or `~/.bashrc`:

```bash
alias checkx='java -jar /path/to/checkxCLI/target/checkx.jar'
```

## Usage

Run without arguments for interactive shell, or pass commands directly:

```bash
checkx              # interactive shell
```

Or:
```bash
java -jar target/checkx.jar
```

This opens a Unix-style command shell:
```shell
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

| Command | Aliases | Description |
|---|---|---|
| `daily` | `today` | Show today's habits & weekly calendar |
| `list` | `habits`, `all` | List all habits |
| `stats` | `statistics` | Show statistics |
| `stats daily` | | Today's stats |
| `stats all` | | All-time stats |
| `add [name]` | `new` | Add new habit with icon selector |
| `add [name], [comment]` | | Add habit with a comment |
| `done [name]` | `complete` | Complete a habit |
| `undone [name]` | `undo` | Revert today's completion |
| `edit [name]` | `rename` | Edit name, icon or comment |
| `delete [name]` | `remove`, `rm` | Delete a habit |
| `help` | `?` | Show all commands |
| `clear` | `cls` | Clear screen |
| `exit` | `quit`, `q` | Exit CheckX |

> Partial names work: `done ex` finds `Exercise`


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

## Roadmap

### Version 1.1 (Done)
- [x] Edit habit name/icon/comment
- [x] Delete habits
- [x] Custom habit icons (icon selector)
- [x] Undo completion
- [x] Partial name search
- [x] Weekly calendar view
- [x] Habit comments

### Version 2.0
- [ ] Pomodoro timer integration
- [ ] Weekly/monthly reports
- [ ] Export data (CSV/JSON)



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

