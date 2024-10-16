check:
	@echo "Running Check (checkstyle, lint, detekt & test)."
	./gradlew check
	@echo "\nAll checks completed!"

test:
	@echo "Running Unit Tests."
	./gradlew test
	@echo "\nAll Unit Tests completed!"

install-hooks:
	cp hooks/pre-commit .git/hooks/pre-commit
	cp hooks/pre-push .git/hooks/pre-push
	chmod +x .git/hooks/pre-commit
	chmod +x .git/hooks/pre-push
