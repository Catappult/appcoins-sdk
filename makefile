check:
	@echo "Running Codestyle verifications (checkstyle, lint & detekt)."
	./gradlew checkstyle lint detekt
	@echo "\nAll checks completed!"

install-hooks:
	cp hooks/pre-push .git/hooks/pre-push
	chmod +x .git/hooks/pre-push
